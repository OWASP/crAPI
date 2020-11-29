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
