package controllers

import (
	"net/http"

	"crapi.proj/goservice/api/config"
	"crapi.proj/goservice/api/responses"
)

type Server config.Server

//Home API is for testing without token
func (server *Server) Home(w http.ResponseWriter, r *http.Request) {
	responses.JSON(w, http.StatusOK, "Welcome To This crAPI API")

}
