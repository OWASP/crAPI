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
	"fmt"
	"os"
	"time"

	"crapi.community/graphql.grpc/grpc/models"
	pb "crapi.community/graphql.grpc/grpc/proto"
	"github.com/jinzhu/gorm"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// initialize coupons data
var coupons = []pb.Coupon{
	pb.Coupon{
		CouponCode: "TRAC075",
		Amount:     "75",
		CreatedAt:  timestamppb.New(time.Now()),
	},
	pb.Coupon{
		CouponCode: "TRAC065",
		Amount:     "65",
		CreatedAt:  timestamppb.New(time.Now()),
	},
	pb.Coupon{
		CouponCode: "TRAC125",
		Amount:     "125",
		CreatedAt:  timestamppb.New(time.Now()),
	},
}

// initialize Post data
var posts = []pb.Post{
	pb.Post{
		Title:   "Title 1",
		Content: "Hello world 1",
	},
	pb.Post{
		Title:   "Title 2",
		Content: "Hello world 2",
	},
	pb.Post{
		Title:   "Title 3",
		Content: "Hello world 3",
	},
}
var emails = [3]string{"adam007@example.com", "pogba006@example.com", "robot001@example.com"}

func LoadMongoData(mongoClient *mongo.Client, db *gorm.DB) {
	var couponResult interface{}
	var postResult interface{}
	collection := mongoClient.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")
	// get a MongoDB document using the FindOne() method
	err := collection.FindOne(context.TODO(), bson.D{}).Decode(&couponResult)
	if err != nil {
		println("There is no existing data in coupons")
		for i, _ := range coupons {
			couponData, err := collection.InsertOne(context.TODO(), coupons[i])
			fmt.Println(couponData, err)
		}
	}
	postCollection := mongoClient.Database(os.Getenv("MONGO_DB_NAME")).Collection("post")
	er := postCollection.FindOne(context.TODO(), bson.D{}).Decode(&postResult)
	if er != nil {
		println("There is no existing data posts")
		for j, _ := range posts {
			models.FindAuthorByEmail(emails[j], db)
			p := models.Convert_pb_Graph_post(&posts[j])
			p = models.PrepareNewPost(p)
			p_pb := models.Convert_Graph_pb_post(p)
			println("Saving posts to mongodb")
			models.SavePost(mongoClient, p_pb)
		}
	}
}
