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
	"html"
	"log"
	"strings"
	"time"

	"github.com/lithammer/shortuuid"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

//Post Field
type Post struct {
	ID        string     `gorm:"primary_key;auto_increment" json:"id"`
	Title     string     `gorm:"size:255;not null;unique" json:"title"`
	Content   string     `gorm:"size:255;not null;" json:"content"`
	Author    Author       `json:"author"`
	Comments  []Comments `json:"comments"`
	AuthorID  uint64     `sql:"type:int REFERENCES users(id)" json:"authorid"`
	CreatedAt time.Time
}

//Prepare initialize data
func (post *Post) Prepare() {
	post.ID = shortuuid.New()
	post.Title = html.EscapeString(strings.TrimSpace(post.Title))
	post.Content = html.EscapeString(strings.TrimSpace(post.Content))
	post.Author = Prepare()
	post.AuthorID = autherID
	post.Comments = []Comments{}
	post.CreatedAt = time.Now()
}

type PostsResponse struct {
	Posts []Post `json:"posts"`
	NextOffset *int64 `json:"next_offset"`
	PrevOffset *int64 `json:"previous_offset"`
	Total int `json:"total"`
}

//Validate data of post
func (post *Post) Validate() error {

	if post.Title == "" {
		return errors.New("required title")
	}
	if post.Content == "" {
		return errors.New("tequired content")
	}
	if post.AuthorID < 1 {
		return errors.New("required author")
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
	u.Picurl = picurl
	return u
}

//SavePost persits data into database
func SavePost(client *mongo.Client, post Post) (Post, error) {

	collection := client.Database("crapi").Collection("post")
	_, err := collection.InsertOne(context.TODO(), post)
	if err != nil {
		log.Println(err)
	}

	return post, err
}

//GetPostByID fetch post by postId
func GetPostByID(client *mongo.Client, ID string) (Post, error) {
	var post Post

	//filter := bson.D{{"name", "Ash"}}
	collection := client.Database("crapi").Collection("post")
	filter := bson.D{{Key: "id", Value: ID}}
	err := collection.FindOne(context.TODO(), filter).Decode(&post)
	if err != nil {
		log.Println(err)
	}

	return post, err

}

//FindAllPost return all recent post
func FindAllPost(client *mongo.Client, offset int64, limit int64) (PostsResponse, error) {
	postList := []Post{}
	var postsResponse PostsResponse = PostsResponse{}
	options := options.Find()
	options.SetSort(bson.D{{Key: "_id", Value: -1}})
	options.SetLimit(limit)
	options.SetSkip(offset)
	ctx := context.Background()
	collection := client.Database("crapi").Collection("post")
	cur, err := collection.Find(ctx, bson.D{}, options)
	if err != nil {
		log.Println("Error in finding posts: ", err)
		return postsResponse, err
	}
	for cur.Next(ctx) {
		var elem Post
		err := cur.Decode(&elem)
		if err != nil {
			log.Println("Error in decoding posts: ", err)
			return postsResponse, err
		}
		postList = append(postList, elem)
	}

	postsResponse.Posts = postList
	// get posts count for pagination
	count, err1 := collection.CountDocuments(context.Background(), bson.D{})
	if err1 != nil {
		log.Println("Error in counting posts: ", err1)
		return postsResponse, err1
	}
	if offset - limit >= 0 {
		tempOffset := offset - limit
		postsResponse.PrevOffset = &tempOffset
	}
	if offset + limit < count {
		tempOffset := offset + limit
		postsResponse.NextOffset = &tempOffset
	}
	postsResponse.Total = len(postList)
	if err = cur.Err(); err != nil {
		log.Println("Error in cursor: ", err)
	}
	return postsResponse, err
}
