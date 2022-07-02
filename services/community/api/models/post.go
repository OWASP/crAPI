/*
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import (
	"context"
	"errors"
	"fmt"
	"html"
	"log"
	"strings"
	"time"

	"github.com/lithammer/shortuuid"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"github.com/jinzhu/gorm"
)

//Post Field
type Post struct {
	ID        string     `gorm:"primarKey" json:"id"`
	Title     string     `gorm:"size:255;not null;unique" json:"title"`
	Content   string     `gorm:"size:255;not null;" json:"content"`
	Author    Author     `gorm:"-" bson:"-" json:"author"`
	Comments  []Comments `json:"comments"`
	AuthorID  uint64     `sql:"type:int REFERENCES users(id)" json:"authorid"`
	CreatedAt time.Time
}

//Prepare initialize data
func (post *Post) Prepare() {
	post.ID = shortuuid.New()
	post.Title = html.EscapeString(strings.TrimSpace(post.Title))
	post.Content = html.EscapeString(strings.TrimSpace(post.Content))
	post.AuthorID = autherID
	post.Comments = []Comments{}
	post.CreatedAt = time.Now()
}

//Validate data of post
func (post *Post) Validate() error {

	if post.Title == "" {
		return errors.New("Required Title")
	}
	if post.Content == "" {
		return errors.New("Required Content")
	}
	if post.AuthorID < 1 {
		return errors.New("Required Author")
	}
	return nil
}

//Prepare initialize Field
func Prepare() Author {
	var u Author
	u.Nickname = nickname
	u.Email = userEmail
	u.VehicleID = vehicleID
	u.CreatedAt = time.Now()
	return u
}

//SavePost persits data into database
func SavePost(client *mongo.Client, post Post) (Post, error) {

	collection := client.Database("crapi").Collection("post")
	_, err := collection.InsertOne(context.TODO(), post)
	if err != nil {
		fmt.Println(err)
	}

	return post, err
}

//GetPostByID fetch post by postId
func GetPostByID(client *mongo.Client, db *gorm.DB, ID string) (Post, error) {
	var post Post
	collection := client.Database("crapi").Collection("post")
	filter := bson.D{{"id", ID}}
	err := collection.FindOne(context.TODO(), filter).Decode(&post)

	author, err2 := GetAuthorByID(post.AuthorID, db)
	if (err2 == nil) {
		post.Author = author
	}
	
	for i := 0; i < len(post.Comments); i++ {
		if post.Comments[i].AuthorID != 0 {
			author, err2 := GetAuthorByID(post.Comments[i].AuthorID, db)
			if err2 == nil {
				post.Comments[i].Author = author
			}
		}
	}
	return post, err

}

//FindAllPost return all recent post
func FindAllPost(client *mongo.Client, db *gorm.DB) ([]interface{}, error) {

	options := options.Find()
	options.SetSort(bson.D{{"_id", -1}})
	options.SetLimit(10)
	collection := client.Database("crapi").Collection("post")
	cur, err := collection.Find(context.Background(), bson.D{}, options)
	if err != nil {
		log.Println(err)
	}
	fmt.Println(cur)
	var list = make([]interface{}, 0)
	defer cur.Close(context.Background())
	for cur.Next(context.Background()) {
		result := &Post{}
		err := cur.Decode(result)

		if err != nil {
			log.Println(err)
			return nil, err
		}
		author, err2 := GetAuthorByID(result.AuthorID, db)
		if (err2 == nil) {
			result.Author = author
		}
		for i := 0; i < len(result.Comments); i++ {
			if result.Comments[i].AuthorID != 0 {
				author, err := GetAuthorByID(result.Comments[i].AuthorID, db)
				if err == nil {
					result.Comments[i].Author = author
				}
			}
		}
		list = append(list, result)
	}
	if err := cur.Err(); err != nil {
		return nil, err
	}

	return list, err
}

//GetAuthorByID check user in database
func GetAuthorByID(id uint64, db *gorm.DB) (Author, error) {
	var author Author
	var err error
	var name string
	var uuid string
	var email string

	row1 := db.Table("user_login").Where("id = ?", id).Select("email").Row()
	row1.Scan(&email)
	author.Email = email
	author.CreatedAt = time.Now()

	//fetch name and picture from for token user
	row2 := db.Table("user_details").Where("id = ?", id).Select("name").Row()
	row2.Scan(&name)
	author.Nickname = name

	row3 := db.Table("vehicle_details").Where("owner_id = ?", id).Select("uuid").Row()
	row3.Scan(&uuid)
	author.VehicleID = uuid

	return author, err
}