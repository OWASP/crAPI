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

package auth

import (
	"bytes"
	"crypto/tls"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net/http"
	"os"
	"strings"

	"crapi.proj/goservice/api/models"
	"crapi.proj/goservice/api/utils"
	jwt "github.com/dgrijalva/jwt-go"
	"github.com/jinzhu/gorm"
)

type Token struct {
	Token string `json:"token"`
}

// ExtractToken return token from Authorization Bearer
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

// ExtractTokenID Verify token either it's valid or not.
// If token is valid we extract username from token Claims.
// Then check that username in postgres database.
func ExtractTokenID(r *http.Request, db *gorm.DB) (uint32, error) {
	http.DefaultTransport.(*http.Transport).TLSClientConfig = &tls.Config{InsecureSkipVerify: true}
	tokenVerifyURL := fmt.Sprintf("http://%s/identity/api/auth/verify", os.Getenv("IDENTITY_SERVICE"))
	tls_enabled, is_tls := os.LookupEnv("TLS_ENABLED")
	if is_tls && utils.IsTrue(tls_enabled) {
		tokenVerifyURL = fmt.Sprintf("https://%s/identity/api/auth/verify", os.Getenv("IDENTITY_SERVICE"))
	}
	tokenString := ExtractToken(r)
	tokenJSON, err := json.Marshal(Token{Token: tokenString})
	if err != nil {
		log.Println(err)
		return 0, err
	}

	resp, err := http.Post(tokenVerifyURL, "application/json", bytes.NewBuffer(tokenJSON))
	if err != nil {
		log.Println(err)
		return 0, err
	}
	defer resp.Body.Close()

	tokenValid := resp.StatusCode == 200
	token, _, err := new(jwt.Parser).ParseUnverified(tokenString, jwt.MapClaims{})
	claims, ok := token.Claims.(jwt.MapClaims)
	if err != nil {
		log.Println(err)
		return 0, err
	}

	if ok && tokenValid {
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

	return 0, errors.New("Unauthorized")
}

// CheckTokenInDB call FindUserByEmail and check that email in postgres database
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

// Pretty display the claims licely in the terminal
func Pretty(data interface{}) {
	b, err := json.MarshalIndent(data, "", " ")
	if err != nil {
		log.Println(err)
		return
	}

	log.Println(string(b))
}

//
