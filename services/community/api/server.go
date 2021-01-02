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

package api

import (
	"log"

	"os"

	"github.com/gorilla/mux"
	"crapi.proj/goservice/api/config"
	"crapi.proj/goservice/api/router"
	"crapi.proj/goservice/api/seed"
	"github.com/joho/godotenv"
)

var server = config.Server{}
var route = router.Server{}

func init() {
	// loads values from .env into the system
	if err := godotenv.Load(); err != nil {
		log.Print("sad .env file found")
	}
}

//
func Run() {

	route.Client = server.InitializeMongo("mongodb", os.Getenv("MONGO_DB_USER"), os.Getenv("MONGO_DB_PASSWORD"), os.Getenv("MONGO_DB_PORT"), os.Getenv("MONGO_DB_HOST"))

	route.DB = server.Initialize("postgres", os.Getenv("DB_USER"), os.Getenv("DB_PASSWORD"), os.Getenv("DB_PORT"), os.Getenv("DB_HOST"), os.Getenv("DB_NAME"))

	seed.LoadMongoData(server.Client, server.DB)

	route.Router = mux.NewRouter()

	server.Router = route.InitializeRoutes()

	route.Run(":8087")

}
