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

package controllers

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"fmt"

	"crapi.proj/goservice/api/models"
	"crapi.proj/goservice/api/responses"
	"go.mongodb.org/mongo-driver/bson"
)

//AddNewCoupon Coupon add coupon in database
//@params ResponseWriter, Request
//Server have database connection
func (s *Server) AddNewCoupon(w http.ResponseWriter, r *http.Request) {
	body, err := ioutil.ReadAll(r.Body)
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
	savedCoupon, er := models.SaveCoupon(s.Client, coupon)
	if er != nil {
		responses.ERROR(w, http.StatusInternalServerError, er)
	}
	if savedCoupon.CouponCode != "" {
		responses.JSON(w, http.StatusOK, "Coupon Added in database")
	}

}

//ValidateCoupon Coupon check coupon in database, if coupon code is valid it returns
//@return
//@params ResponseWriter, Request
//Server have database connection
func (s *Server) ValidateCoupon(w http.ResponseWriter, r *http.Request) {
	
	//coupon := models.CouponBody{}
	var bsonMap bson.M

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		responses.ERROR(w, http.StatusBadRequest, err)
		fmt.Println("No payload for ValidateCoupon", body, err)
		return
	}
	err = json.Unmarshal(body, &bsonMap)
	if err != nil {
		responses.ERROR(w, http.StatusUnprocessableEntity, err)
		fmt.Println("Failed to read json body", err)
		return
	}
	couponData, err := models.ValidateCode(s.Client, s.DB, bsonMap)

	if err != nil {
		fmt.Println("Error fetching Coupon", couponData, err)
		responses.JSON(w, http.StatusInternalServerError, err)
		return
	}
	responses.JSON(w, http.StatusOK, couponData)
}
