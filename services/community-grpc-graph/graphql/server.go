package main

import (
	"errors"
	"log"
	"net/http"
	"os"

	graph "crapi.community/graphql.grpc/graphql"
	"crapi.community/graphql.grpc/graphql/auth"
	"crapi.community/graphql.grpc/graphql/config"
	"crapi.community/graphql.grpc/grpc/responses"
	"github.com/99designs/gqlgen/graphql/handler"
	"github.com/99designs/gqlgen/graphql/playground"
	"github.com/jinzhu/gorm"
)

var server = config.Server{}

const defaultPort = "8087"

var DB *gorm.DB

func SetMiddlewareAuthentication(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Printf("Request URL: %s", r.URL.String())
		_, err := auth.ExtractTokenID(r, server.DB)
		if err != nil {
			responses.ERROR(w, http.StatusUnauthorized, errors.New("Unauthorized"))
			return
		}
		next.ServeHTTP(w, r)
	})
}

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = defaultPort
	}

	server.DB = server.Initialize("postgres", os.Getenv("DB_USER"), os.Getenv("DB_PASSWORD"), os.Getenv("DB_PORT"), os.Getenv("DB_HOST"), os.Getenv("DB_NAME"))
	srv := handler.NewDefaultServer(graph.NewExecutableSchema(graph.Config{Resolvers: &graph.Resolver{}}))

	http.Handle("/health", playground.Handler("GraphQL playground", "/query"))
	http.Handle("/community/query", SetMiddlewareAuthentication(srv))
	http.Handle("/workshop/query", SetMiddlewareAuthentication(srv))
	log.Printf("connect to http://localhost:%s/ for GraphQL playground", port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}
