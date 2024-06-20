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

package seed

import (
	"context"
	"log"
	"os"
	"time"

	"crapi.proj/goservice/api/models"
	"github.com/jinzhu/gorm"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

//initialize coupons data
var coupons = []models.Coupon{
	models.Coupon{
		CouponCode: "TRAC075",
		Amount:     "75",
		CreatedAt:  time.Now(),
	},
	models.Coupon{
		CouponCode: "TRAC065",
		Amount:     "65",
		CreatedAt:  time.Now(),
	},
	models.Coupon{
		CouponCode: "TRAC125",
		Amount:     "125",
		CreatedAt:  time.Now(),
	},
}

//initialize Post data
var posts = []models.Post{
	{
		Title:   "Title 1",
		Content: "Hello world 1",
	},
	{
		Title:   "Title 2",
		Content: "Hello world 2",
	},
	{
		Title:   "Title 3",
		Content: "Hello world 3",
	},
}
var emails = [3]string{"adam007@example.com", "pogba006@example.com", "robot001@example.com"}

//
func LoadMongoData(mongoClient *mongo.Client, db *gorm.DB) {
	var couponResult interface{}
	var postResult interface{}
	collection := mongoClient.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")
	// get a MongoDB document using the FindOne() method
	err := collection.FindOne(context.TODO(), bson.D{}).Decode(&couponResult)
	if err != nil {
		for i := range coupons {
			couponData, err := collection.InsertOne(context.TODO(), coupons[i])
			log.Println(couponData, err)
		}
	}
	postCollection := mongoClient.Database(os.Getenv("MONGO_DB_NAME")).Collection("post")
	er := postCollection.FindOne(context.TODO(), bson.D{}).Decode(&postResult)
	if er != nil {
		for j := range posts {
			author, err := models.FindAuthorByEmail(emails[j], db)
			if err != nil {
				log.Println("Error finding author", err)
				continue
			}
			log.Println(author)
			posts[j].Prepare()
			postData, err := models.SavePost(mongoClient, posts[j]) // Assign the returned values to separate variables
			if err != nil {
				log.Println("Error saving post", err)
			}
			log.Println(postData) // Use the returned values as needed
		}
	}
	log.Println("Data seeded successfully")
}
