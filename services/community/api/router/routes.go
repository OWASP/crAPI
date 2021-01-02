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

package router

import (
	"fmt"
	"net/http"

	"github.com/gorilla/mux"
	"crapi.proj/goservice/api/config"
	"crapi.proj/goservice/api/controllers"
	"crapi.proj/goservice/api/middlewares"
)

type Server config.Server

var controller = controllers.Server{}

//initializeRoutes initialize routes of url with Authentication or without Authentication
func (server *Server) InitializeRoutes() *mux.Router {

	controller.DB = server.DB

	controller.Client = server.Client

	server.Router.Use(middlewares.AccessControlMiddleware)
	// Post Route
	server.Router.HandleFunc("/community/api/v2/community/posts/recent", middlewares.SetMiddlewareJSON(middlewares.SetMiddlewareAuthentication(controller.GetPost, server.DB))).Methods("GET", "OPTIONS")

	server.Router.HandleFunc("/community/api/v2/community/posts/{postID}", middlewares.SetMiddlewareJSON(middlewares.SetMiddlewareAuthentication(controller.GetPostByID, server.DB))).Methods("GET", "OPTIONS")

	server.Router.HandleFunc("/community/api/v2/community/posts", middlewares.SetMiddlewareJSON(middlewares.SetMiddlewareAuthentication(controller.AddNewPost, server.DB))).Methods("POST", "OPTIONS")

	server.Router.HandleFunc("/community/api/v2/community/posts/{postID}/comment", middlewares.SetMiddlewareJSON(middlewares.SetMiddlewareAuthentication(controller.Comment, server.DB))).Methods("POST", "OPTIONS")

	//Coupon Route
	server.Router.HandleFunc("/community/api/v2/coupon/new-coupon", middlewares.SetMiddlewareJSON(middlewares.SetMiddlewareAuthentication(controller.AddNewCoupon, server.DB))).Methods("POST", "OPTIONS")

	server.Router.HandleFunc("/community/api/v2/coupon/validate-coupon", middlewares.SetMiddlewareJSON(middlewares.SetMiddlewareAuthentication(controller.ValidateCoupon, server.DB))).Methods("POST", "OPTIONS")

	//Health
	server.Router.HandleFunc("/community/home", middlewares.SetMiddlewareJSON(controller.Home)).Methods("GET")
	return server.Router
}

//
func (server *Server) Run(addr string) {
	fmt.Println("Listening to port 8087")
	fmt.Println(http.ListenAndServe(addr, server.Router))
}
