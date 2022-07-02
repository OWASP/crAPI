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
	"log"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"github.com/jinzhu/gorm"
)

//
type Comments struct {
	ID        string `gorm:"-" bson:"-" json:"-"`
	Content   string `gorm:"size:255;not null;" json:"content"`
	CreatedAt time.Time
	Author    Author `gorm:"-" bson:"-" json:"author"`
	AuthorID  uint64 `sql:"type:int REFERENCES users(id)" json:"authorid"`
}

//CommentOnPost Add comment in post by id.
func CommentOnPost(client *mongo.Client, db *gorm.DB, postComment Comments) (Post, error) {
	var comments Comments
	//Take data from Database by postId
	preData, err := GetPostByID(client, db, postComment.ID)
	updatePost := preData
	if err != nil {
		log.Println(err)
	} else {
		comments.Content = postComment.Content
		comments.AuthorID = autherID
		comments.CreatedAt = time.Now()
		//Add comment in post
		updatePost.Comments = append(updatePost.Comments, comments)

		update := bson.D{{"$set", bson.D{{"comments", updatePost.Comments}}}}

		collection := client.Database("crapi").Collection("post")

		_, err = collection.UpdateOne(context.TODO(), preData, update)
		if err != nil {
			log.Println(err)
		}
	}

	for i := 0; i < len(updatePost.Comments); i++ {
		if updatePost.Comments[i].AuthorID != 0 {
			author, err := GetAuthorByID(updatePost.Comments[i].AuthorID, db)
			if err == nil {
				updatePost.Comments[i].Author = author
			}
		}
	}
	return updatePost, err
}
