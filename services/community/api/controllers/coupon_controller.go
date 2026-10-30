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

// Coupon handlers behind /community/api/v2/coupon.
//
// AddNewCoupon stores a coupon code and amount in MongoDB. ValidateCoupon takes
// the request body as a raw BSON document and passes it straight into a Mongo
// query (models.ValidateCode), which is the NoSQL injection challenge. The
// coupons written here are what Workshop's ApplyCouponView reads from the same
// Mongo collection when a user redeems a code in the shop.

package controllers

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"

	"crapi.proj/goservice/api/models"
	"crapi.proj/goservice/api/responses"
	"go.mongodb.org/mongo-driver/bson"
)

// AddNewCoupon Coupon add coupon in database
// @params ResponseWriter, Request
// Server have database connection
func (s *Server) AddNewCoupon(w http.ResponseWriter, r *http.Request) {
	body, err := io.ReadAll(r.Body)
	defer func() {
		if err := r.Body.Close(); err != nil {
			log.Println("Error closing request body:", err)
		}
	}()
	if err != nil {
		responses.ERROR(w, http.StatusBadRequest, err)
		return
	}
	coupon := models.Coupon{}
	err = json.Unmarshal(body, &coupon)
	if err != nil {
		responses.ERROR(w, http.StatusBadRequest, err)
		return
	}
	coupon.Prepare()

	existingCoupon, err := models.ValidateCode(s.Client, s.DB, bson.M{"coupon_code": coupon.CouponCode})
	if err == nil && existingCoupon.CouponCode != "" {
		responses.ERROR(w, http.StatusConflict, fmt.Errorf("coupon code already exists"))
		return
	}

	savedCoupon, er := models.SaveCoupon(s.Client, coupon)
	if er != nil {
		responses.ERROR(w, http.StatusInternalServerError, er)
		return
	}
	if savedCoupon.CouponCode != "" {
		responses.JSON(w, http.StatusOK, "Coupon added in database!")
	}

}

// ValidateCoupon Coupon check coupon in database, if coupon code is valid it returns
// @return
// @params ResponseWriter, Request
// Server have database connection
func (s *Server) ValidateCoupon(w http.ResponseWriter, r *http.Request) {

	//coupon := models.CouponBody{}
	var bsonMap bson.M

	body, err := io.ReadAll(r.Body)
	defer func() {
		if err := r.Body.Close(); err != nil {
			log.Println("Error closing request body:", err)
		}
	}()
	if err != nil {
		responses.ERROR(w, http.StatusBadRequest, err)
		log.Println("No payload for ValidateCoupon", string(body), err)
		return
	}
	err = json.Unmarshal(body, &bsonMap)
	if err != nil {
		responses.ERROR(w, http.StatusUnprocessableEntity, err)
		log.Println("Failed to read json body", err)
		return
	}
	couponData, err := models.ValidateCode(s.Client, s.DB, bsonMap)

	if err != nil {
		log.Println("Error fetching Coupon", couponData, err)
		responses.JSON(w, http.StatusInternalServerError, err)
		return
	}
	responses.JSON(w, http.StatusOK, couponData)
}
