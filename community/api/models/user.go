package models

import (
	"errors"
	"strings"
	"time"

	"github.com/badoux/checkmail"
	"github.com/jinzhu/gorm"
	"golang.org/x/crypto/bcrypt"
)



var autherID uint64
var nickname string
var userEmail string

//var picurl string
var vehicleID string

//Author model
type Author struct {
	Nickname  string    `gorm:"size:255;not null;unique" json:"nickname"`
	Email     string    `gorm:"size:100;not null;unique" json:"email"`
	VehicleID string    `gorm:"size:100;not null;unique" json:"vehicleid"`
	Picurl    string    `gorm:"size:100;not null;unique" json:"profile_pic_url"`
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
			return errors.New("Required Nickname")
		}
		if u.Email == "" {
			return errors.New("Required Email")
		}
		if err := checkmail.ValidateFormat(u.Email); err != nil {
			return errors.New("Invalid Email")
		}
		return nil

	case "login":
		if u.Nickname == "" {
			return errors.New("Required Nickname")
		}
		if u.Email == "" {
			return errors.New("Required Email")
		}
		if err := checkmail.ValidateFormat(u.Email); err != nil {
			return errors.New("Invalid Email")
		}
		return nil
	default:
		if u.Nickname == "" {
			return errors.New("Required Nickname")
		}
		if u.Email == "" {
			return errors.New("Required Email")
		}
		if err := checkmail.ValidateFormat(u.Email); err != nil {
			return errors.New("Invalid Email")
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

	row.Scan(&id, &number)

	autherID = id
	//fetch name and picture from for token user
	row1 := db.Table("user_details").Where("user_id = ?", id).Select("name, picture").Row()
	row1.Scan(&name, &picture)

	nickname = name
	row2 := db.Table("vehicle_details").Where("owner_id = ?", id).Select("uuid").Row()
	row2.Scan(&uuid)
	vehicleID = uuid
	return number, err
}
