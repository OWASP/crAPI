package config

import (
	"fmt"
	"log"

	"github.com/gorilla/mux"
	"github.com/jinzhu/gorm"
	"go.mongodb.org/mongo-driver/mongo"

	_ "github.com/jinzhu/gorm/dialects/postgres"
)

//
type Server struct {
	Router *mux.Router
	DB     *gorm.DB
	Client *mongo.Client
}

//Initialize Postgres database for token
func (server *Server) Initialize(Dbdriver, DbUser, DbPassword, DbPort, DbHost, DbName string) *gorm.DB {
	var err error

	if Dbdriver == "postgres" {
		DBURL := fmt.Sprintf("host=%s port=%s user=%s dbname=%s sslmode=disable password=%s", DbHost, DbPort, DbUser, DbName, DbPassword)
		server.DB, err = gorm.Open(Dbdriver, DBURL)
		if err != nil {
			log.Fatalf("Cannot connect to %s database", err)

		} else {
			log.Println("We are connected to the %s database", Dbdriver)
		}
	}

	return server.DB
}
