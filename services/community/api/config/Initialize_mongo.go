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
