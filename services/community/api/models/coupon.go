/*
 * Copyright 2020 Traceable, Inc.
 *
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
	"strings"
	"time"
	"github.com/jinzhu/gorm"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/bson"
)

//Coupon
type Coupon struct {
	CouponCode string `bson:"coupon_code" json:"coupon_code"`
	Amount     string `json:"amount"`
	CreatedAt  time.Time
}

//
func (c *Coupon) Prepare() {
	c.CouponCode = html.EscapeString(strings.TrimSpace(c.CouponCode))
	c.Amount = html.EscapeString(strings.TrimSpace(c.Amount))
	c.CreatedAt = time.Now()

}

//Validate coupon
func (c *Coupon) Validate() error {

	if c.CouponCode == "" {
		return errors.New("Required Coupon Code")
	}
	if c.Amount == "" {
		return errors.New("Required Coupon Amount")
	}

	return nil
}

//SaveCoupon save coupon database.
func SaveCoupon(client *mongo.Client, coupon Coupon) (Coupon, error) {

	// Get a handle for your collection
	collection := client.Database("crapi").Collection("coupon")

	// Insert a single document
	insertResult, err := collection.InsertOne(context.TODO(), coupon)
	if err != nil {
		fmt.Println(err)
	}
	fmt.Println("Inserted a single document: ", insertResult.InsertedID)

	return coupon, err
}

//ValidateCode write query in mongodb for check coupon code
func ValidateCode(client *mongo.Client, db *gorm.DB, bsonMap bson.M) (Coupon, error) {
	var result Coupon

	// Get a handle for your collection
	collection := client.Database("crapi").Collection("coupons")

	err := collection.FindOne(context.TODO(), bsonMap).Decode(&result)
	if err != nil {
		return result, err
	}
	return result, err
}
