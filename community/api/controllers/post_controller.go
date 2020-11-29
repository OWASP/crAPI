package controllers

import (
	"encoding/json"
	"io/ioutil"
	"net/http"

	"github.com/gorilla/mux"
	"crapi.proj/goservice/api/models"
	"crapi.proj/goservice/api/responses"
)

//AddNewPost add post in database,
//@return HTTP Status
//@params ResponseWriter, Request
//Server have database connection
func (s *Server) AddNewPost(w http.ResponseWriter, r *http.Request) {

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		responses.ERROR(w, http.StatusBadRequest, err)
		return
	}
	post := models.Post{}

	err = json.Unmarshal(body, &post)
	if err != nil {
		responses.ERROR(w, http.StatusBadRequest, err)
		return
	}
	post.Prepare()

	_, err = models.SavePost(s.Client, post)
	if err != nil {
		responses.ERROR(w, http.StatusInternalServerError, err)
	}

	responses.JSON(w, http.StatusOK, "Post Added in database")
}

//GetPostByID fetch the post by ID,
//@return HTTP Status
//@params ResponseWriter, Request
//Server have database connection
func (s *Server) GetPostByID(w http.ResponseWriter, r *http.Request) {

	vars := mux.Vars(r)
	//var autherID uint64
	GetPost, er := models.GetPostByID(s.Client, vars["postID"])
	if er != nil {
		responses.ERROR(w, http.StatusBadRequest, er)
	}

	responses.JSON(w, http.StatusOK, GetPost)

}

//GetPost Vulnerabilities
func (s *Server) GetPost(w http.ResponseWriter, r *http.Request) {
	//post := models.Post{}

	posts, err := models.FindAllPost(s.Client)
	if err != nil {
		responses.ERROR(w, http.StatusInternalServerError, err)
		return
	}
	responses.JSON(w, http.StatusOK, posts)
}

//Comment will add comment in perticular post,
//@return HTTP Post Object
//@params ResponseWriter, Request
//Server have database connection
func (s *Server) Comment(w http.ResponseWriter, r *http.Request) {

	vars := mux.Vars(r)
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		responses.ERROR(w, http.StatusBadRequest, err)
		return
	}
	comment := models.Comments{}

	err = json.Unmarshal(body, &comment)
	if err != nil {
		responses.ERROR(w, http.StatusBadRequest, err)
		return
	}
	if vars["postID"] == "" {
		responses.ERROR(w, http.StatusBadRequest, err)
		return
	}
	comment.ID = vars["postID"]
	postData, er := models.CommentOnPost(s.Client, comment)
	if er != nil {
		responses.ERROR(w, http.StatusInternalServerError, er)
		return
	}
	responses.JSON(w, http.StatusOK, postData)
}
