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

// Author model: the slice of a user that gets embedded in posts and comments
// (nickname, email, vehicle id, profile picture URL).
//
// Users are not created here. FindAuthorByEmail joins the Identity-owned
// user_details / user_login / vehicle_details tables in the shared Postgres
// database, and the package-level variables it fills are what Prepare() in
// post.go copies into each new post. Embedding the email and vehicle id is
// deliberate: it feeds the excessive data exposure challenge.

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

// CurrentUserEmail is the email of the user whose token the authentication
// middleware last verified (see FindAuthorByEmail).
func CurrentUserEmail() string {
	return userEmail
}

// AuthorPictures returns the current profile picture, as a data URL, of each
// email that has one, read from the user tables Identity owns.
func AuthorPictures(emails []string, db *gorm.DB) map[string]string {
	pictures := map[string]string{}
	if len(emails) == 0 {
		return pictures
	}
	rows, err := db.Table("user_login").
		Select("user_login.email, lo_get(user_details.picture)").
		Joins("JOIN user_details ON user_details.user_id = user_login.id").
		Where("user_login.email IN (?)", emails).
		Where("user_details.picture IS NOT NULL").
		Rows()
	if err != nil {
		log.Println("Error in AuthorPictures", err)
		return pictures
	}
	defer rows.Close()
	for rows.Next() {
		var email string
		var picture []byte
		if err := rows.Scan(&email, &picture); err != nil {
			log.Println("Error in AuthorPictures", err)
			continue
		}
		if len(picture) > 0 {
			pictures[email] = "data:image/jpeg;base64," + base64.StdEncoding.EncodeToString(picture)
		}
	}
	return pictures
}

// RefreshAuthorPictures replaces the profile picture that was copied into each
// post and comment when it was written with the author's current one, so a
// picture uploaded after posting still shows up in the forum.
func RefreshAuthorPictures(db *gorm.DB, posts []Post) {
	emails := []string{}
	seen := map[string]bool{}
	collect := func(email string) {
		if email != "" && !seen[email] {
			seen[email] = true
			emails = append(emails, email)
		}
	}
	for i := range posts {
		collect(posts[i].Author.Email)
		for j := range posts[i].Comments {
			collect(posts[i].Comments[j].Author.Email)
		}
	}
	pictures := AuthorPictures(emails, db)
	for i := range posts {
		if picture, ok := pictures[posts[i].Author.Email]; ok {
			posts[i].Author.Picurl = picture
		}
		for j := range posts[i].Comments {
			if picture, ok := pictures[posts[i].Comments[j].Author.Email]; ok {
				posts[i].Comments[j].Author.Picurl = picture
			}
		}
	}
}
