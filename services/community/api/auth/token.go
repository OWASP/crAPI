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

package auth

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"strings"

	jwt "github.com/dgrijalva/jwt-go"
	"crapi.proj/goservice/api/models"
	"github.com/jinzhu/gorm"
)

//ExtractToken return token from Authorization Bearer
func ExtractToken(r *http.Request) string {
	keys := r.URL.Query()
	token := keys.Get("token")
	if token != "" {
		return token
	}
	bearerToken := r.Header.Get("Authorization")
	if len(strings.Split(bearerToken, " ")) == 2 {
		return strings.Split(bearerToken, " ")[1]
	}
	return ""
}

//ExtractTokenID Verify token either it's valid or not.
//If token is valid we extract username from token Claims.
//Then check that username in postgres database.
func ExtractTokenID(r *http.Request, db *gorm.DB) (uint32, error) {

	tokenString := ExtractToken(r)
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("Unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(os.Getenv("JWT_SECRET")), nil
	})
	if err != nil {
		return 0, err
	}
	claims, ok := token.Claims.(jwt.MapClaims)
	if ok && token.Valid {
		name := claims["sub"]
		if name != nil {
			//converting name interface to string
			email := fmt.Sprintf("%v", name)
			// checking username in postgres databse.
			err := CheckTokenInDB(email, db)
			return 0, err
		}
		var uid uint32
		return uint32(uid), nil
	}
	return 0, nil
}

//CheckTokenInDB call FindUserByEmail and check that email in postgres database
func CheckTokenInDB(username string, db *gorm.DB) error {
	email := fmt.Sprintf("%v", username)
	//Calling user model for database query
	num, err := models.FindAuthorByEmail(email, db)

	if num != nil {
		return nil
	}
	if err != nil {
		return err
	}
	return err

}

//Pretty display the claims licely in the terminal
func Pretty(data interface{}) {
	b, err := json.MarshalIndent(data, "", " ")
	if err != nil {
		log.Println(err)
		return
	}

	fmt.Println(string(b))
}

//
