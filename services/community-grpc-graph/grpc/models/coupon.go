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
	"encoding/json"
	"fmt"
	"html"
	"log"
	"os"
	"strings"
	"time"

	"crapi.community/graphql.grpc/graph/model"

	pb "crapi.community/graphql.grpc/grpc/proto"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

func PrepareNewCoupon(coupon model.CouponInput) model.Coupon {
	Coupon := model.Coupon{
		CouponCode: html.EscapeString(strings.TrimSpace(coupon.CouponCode)),
		Amount:     html.EscapeString(strings.TrimSpace(coupon.Amount)),
		CreatedAt:  time.Now(),
	}
	return Coupon
}

// SaveCoupon persits data into database
func SaveCoupon(client *mongo.Client, coupon *pb.Coupon) (*pb.CreateCouponResponse, error) {

	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")
	_, err := collection.InsertOne(context.TODO(), coupon)
	if err != nil {
		println("Error while inserting coupon into collection")
		fmt.Println(err)
	}

	res := &pb.CreateCouponResponse{
		Success: true,
	}
	return res, nil
}

// Get an array of all coupons having matching couponcode
func GetCoupons(client *mongo.Client, in []string) (*pb.GetCouponsResponse, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")
	var coupons [](*pb.Coupon)
	for i := 0; i < len(in); i++ {
		filter := bson.D{{"coupon_code", in[i]}}
		var result *pb.Coupon
		err := collection.FindOne(context.TODO(), filter).Decode(&result)
		if err != nil {
			log.Println("Fetching documents from collection failed, %v", err)
		} else {
			result.CouponCode = in[i]
			coupons = append(coupons, result)
		}
	}
	res := &pb.GetCouponsResponse{
		Coupons: coupons,
	}
	return res, nil
}

// Validate coupon
func ValidateCoupon(client *mongo.Client, code *pb.ValidateCouponRequest) (*pb.ValidateCouponResponse, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")
	jsonBody := fmt.Sprintf("{\"coupon_code\": %s}", code.GetCouponCode())
	var bsonMap bson.M
	err := json.Unmarshal([]byte(jsonBody), &bsonMap)
	var result *pb.Coupon
	err = collection.FindOne(context.TODO(), bsonMap).Decode(&result)
	if err != nil {
		log.Println("Fetching documents from collection failed, %v", err)
		return nil, err
	}
	return &pb.ValidateCouponResponse{
		Coupon: result,
	}, nil
}
