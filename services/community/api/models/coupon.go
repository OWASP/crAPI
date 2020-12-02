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
