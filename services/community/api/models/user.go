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
	"errors"
	"log"
	"strings"
	"time"

	"github.com/badoux/checkmail"
	"github.com/jinzhu/gorm"
	"golang.org/x/crypto/bcrypt"

	"encoding/base64"
)



var autherID uint64
var nickname string
var userEmail string

var picurl string
var vehicleID string

//Author model
type Author struct {
	Nickname  string    `gorm:"size:255;not null;unique" json:"nickname"`
	Email     string    `gorm:"size:100;not null;unique" json:"email"`
	VehicleID string    `gorm:"size:100;not null;unique" json:"vehicleid"`
	Picurl    string    `gorm:"size:30000;not null;unique" json:"profile_pic_url"`
	CreatedAt time.Time `gorm:"default:CURRENT_TIMESTAMP" json:"created_at"`
}

//Hash for password
func Hash(password string) ([]byte, error) {
	return bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
}

//VerifyPassword compare password and hashcode
func VerifyPassword(hashedPassword, password string) error {
	return bcrypt.CompareHashAndPassword([]byte(hashedPassword), []byte(password))
}

//Prepare initilize user object

//Validate Author
func (u *Author) Validate(action string) error {
	switch strings.ToLower(action) {
	case "update":
		if u.Nickname == "" {
			return errors.New("required nickname")
		}
		if u.Email == "" {
			return errors.New("required email")
		}
		if err := checkmail.ValidateFormat(u.Email); err != nil {
			return errors.New("invalid email")
		}
		return nil

	case "login":
		if u.Nickname == "" {
			return errors.New("required nickname")
		}
		if u.Email == "" {
			return errors.New("required email")
		}
		if err := checkmail.ValidateFormat(u.Email); err != nil {
			return errors.New("invalid email")
		}
		return nil
	default:
		if u.Nickname == "" {
			return errors.New("required nickname")
		}
		if u.Email == "" {
			return errors.New("required email")
		}
		if err := checkmail.ValidateFormat(u.Email); err != nil {
			return errors.New("invalid email")
		}
		return nil
	}
}

//FindAuthorByEmail check user in database
func FindAuthorByEmail(email string, db *gorm.DB) (*uint64, error) {
	var err error
	var id uint64
	var number *uint64
	var name string
	var picture []byte
	var uuid string
	userEmail = email

	//fetch id and number from for token user
	row := db.Table("user_login").Where("email LIKE ?", email).Select("id,number").Row()

	err = row.Scan(&id, &number)
	if err != nil {
		log.Println("Error in FindAuthorByEmail", err)
	}

	autherID = id
	//fetch name and picture from for token user
	row1 := db.Table("user_details").Where("user_id = ?", id).Select("name, lo_get(picture)").Row()
	err = row1.Scan(&name, &picture)
	if err != nil {
		log.Println("Error in FindAuthorByEmail", err)
	}
	if len(picture) > 0 {
		picurl = "data:image/jpeg;base64," + base64.StdEncoding.EncodeToString(picture)
	}
	nickname = name
	row2 := db.Table("vehicle_details").Where("owner_id = ?", id).Select("uuid").Row()
	err = row2.Scan(&uuid)
	if err != nil {
		log.Println("Error in FindAuthorByEmail", err)
	}
	vehicleID = uuid
	return number, err
}
