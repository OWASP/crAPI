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
	"go.mongodb.org/mongo-driver/mongo/options"
)

func PrepareNewCoupon(coupon model.CouponInput) model.Coupon {
	Coupon := model.Coupon{
		CouponCode: html.EscapeString(strings.TrimSpace(coupon.CouponCode)),
		Amount:     html.EscapeString(strings.TrimSpace(coupon.Amount)),
		CreatedAt:  time.Now(),
	}
	return Coupon
}

func PrepareUpdatedCoupon(coupon model.CouponInput) model.Coupon {
	// using pointers and reference for handling a warning
	uc := &model.Coupon{
		CouponCode: coupon.CouponCode,
		Amount:     coupon.Amount,
		CreatedAt:  time.Now(),
	}
	return *uc
}

// Validate coupon
func ValidateCoupon(client *mongo.Client, couponCode string) (*model.Coupon, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")
	filter := bson.D{{"couponcode", couponCode}}
	var result *pb.Coupon
	err := collection.FindOne(context.TODO(), filter).Decode(&result)
	if err != nil {
		log.Println("Fetching documents from collection failed, %v", err)
		return nil, err
	}

	return &model.Coupon{
		CouponCode: result.GetCouponCode(),
		Amount:     result.GetAmount(),
		CreatedAt:  Convert_to_Time(result.GetCreatedAt()),
	}, nil
}

// SaveCoupon persits data into database
func SaveCoupon(client *mongo.Client, coupon *pb.Coupon) (*pb.CreateCouponResponse, error) {

	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")
	// Modify the BSON document to replace "couponcode" with "coupon_code"
	doc, err := bson.Marshal(coupon)
	if err != nil {
		println("Error while marshaling coupon into BSON document")
		fmt.Println(err)
	}
	var result bson.M
	err = bson.Unmarshal(doc, &result)
	if err != nil {
		println("Error while unmarshaling BSON document")
		fmt.Println(err)
	}
	result["coupon_code"] = result["couponcode"]
	delete(result, "couponcode")
	_, err = collection.InsertOne(context.TODO(), result)
	if err != nil {
		println("Error while inserting coupon into collection")
		fmt.Println(err)
	}

	res := &pb.CreateCouponResponse{
		Success: true,
	}
	return res, nil
}

// Update coupon persisting into database
func UpdateCoupon(client *mongo.Client, coupon *pb.Coupon, couponcode string) (*pb.UpdateCouponResponse, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")

	opts := options.Update().SetUpsert(true)
	filter := bson.D{{"couponcode", couponcode}}
	update := bson.D{{"$set", coupon}}

	_, err := collection.UpdateOne(context.TODO(), filter, update, opts)
	if err != nil {
		println("Error while updating by couponcode")
		fmt.Println(err)
	}

	res := &pb.UpdateCouponResponse{
		Success: true,
	}
	return res, nil
}

// Get an array of all coupons having matching couponcode
func GetCoupons(client *mongo.Client, in []string) (*pb.GetCouponsResponse, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")
	var coupons [](*pb.Coupon)
	for i := 0; i < len(in); i++ {
		filter := bson.D{{"couponcode", in[i]}}
		var result *pb.Coupon
		err := collection.FindOne(context.TODO(), filter).Decode(&result)
		if err != nil {
			log.Println("Fetching documents from collection failed, %v", err)
		} else {
			coupons = append(coupons, result)
		}
	}
	res := &pb.GetCouponsResponse{
		Coupons: coupons,
	}
	return res, nil
}

// Get an array of all deleted coupons having matching couponcode
func DeleteCoupons(client *mongo.Client, in []string) (*pb.DeleteCouponsResponse, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("coupons")
	var coupons [](*pb.Coupon)
	for i := 0; i < len(in); i++ {
		filter := bson.D{{"couponcode", in[i]}}
		var result *pb.Coupon
		err_get := collection.FindOne(context.TODO(), filter).Decode(&result)
		if err_get != nil {
			println("Cannot delete coupon with id " + in[i] + " .... Coupon does not exist in database")
			continue
		}
		_, err := collection.DeleteOne(context.TODO(), filter)
		if err != nil {
			log.Println("Deleting documents from collection failed, %v", err)
		} else {
			coupons = append(coupons, result)
		}
	}
	res := &pb.DeleteCouponsResponse{
		DeletedCoupons: coupons,
	}
	return res, nil
}
