package config

import (
	"context"
	"fmt"
	"log"

	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

//InitializeMongo initilize mongo database
func (server *Server) InitializeMongo(DbDriver, DbUser string, DbPassword string, DbPort, DbHost string) *mongo.Client {

	if DbDriver == "mongodb" {
		// Set client options
		var err error

		DBURL := fmt.Sprintf("mongodb://%s:%s@%s:%s", DbUser, DbPassword, DbHost, DbPort)
		clientOptions := options.Client().ApplyURI(DBURL)

		server.Client, err = mongo.Connect(context.TODO(), clientOptions)
		if err != nil {
			log.Fatal(err)
		}

		// Check the connection
		err = server.Client.Ping(context.TODO(), nil)
		if err != nil {
			log.Fatal(err)
		}

		fmt.Println("Connected to MongoDB!")

	}

	return server.Client
}
