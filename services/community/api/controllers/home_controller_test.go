package controllers

import (
	"net/http"
	"testing"
)

func TestServer_Home(t *testing.T) {
	type args struct {
		w http.ResponseWriter
		r *http.Request
	}
	tests := []struct {
		name   string
		server *Server
		args   args
	}{
		// TODO: Add test cases.
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			tt.server.Home(tt.args.w, tt.args.r)
		})
	}
}
