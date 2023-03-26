package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"hash/fnv"
	"log"
	"math/rand"
	"net/http"
	"time"

	"github.com/bradfitz/iter"
	"github.com/dustin/go-humanize"
	"github.com/jaswdr/faker"
)

type VINOwner struct {
	VIN              string `json:"vin"`
	Rank             string `json:"rank"`
	Name             string `json:"name"`
	Phone            string `json:"phone"`
	Email            string `json:"email"`
	SSN              string `json:"ssn"`
	Address          string `json:"address"`
	RegistrationID   string `json:"registration_id"`
	RegistrationDate string `json:"registration_date"`
}

func HelloServer(w http.ResponseWriter, req *http.Request) {
	w.Header().Set("Content-Type", "text/plain")
	w.Write([]byte("VinHistory.\n"))
}

func GetOwners(w http.ResponseWriter, r *http.Request) {
	vin := r.URL.Query().Get("vin")
	if vin == "" {
		http.Error(w, "vin param is required", http.StatusBadRequest)
		return
	}
	h := fnv.New32a()
	h.Write([]byte(vin))
	seed := int64(h.Sum32())
	src := rand.NewSource(seed)
	fake := faker.NewWithSeed(src)
	fmt.Printf("Vehicle: %+v : Seed %d\n", vin, seed)
	w.Header().Set("Content-Type", "application/json")
	owners := []VINOwner{}
	ftime := fake.Time()
	limit := fake.IntBetween(0, 3)
	for i := range iter.N(limit) {
		var owner VINOwner
		owner.VIN = vin
		owner.Rank = humanize.Ordinal(i + 1)
		p := fake.Person()
		c := p.Contact()
		owner.Name = p.Name()
		owner.Phone = c.Phone
		owner.Email = c.Email
		owner.SSN = p.SSN()
		owner.Address = fake.Address().Address()
		owner.RegistrationID = fake.Hash().MD5()
		owner.RegistrationDate = ftime.ISO8601(time.Now().AddDate(-1*i, 0, 0))
		owners = append(owners, owner)
	}
	response, _ := json.Marshal(owners)
	w.Write(response)
}

func main() {
	port := flag.Int("port", 443, "port number")
	flag.Parse()
	http.HandleFunc("/", HelloServer)
	http.HandleFunc("/vin/ownership", GetOwners)
	listen_addr := fmt.Sprintf(":%d", *port)
	log.Printf("Listening on %s", listen_addr)
	err := http.ListenAndServeTLS(listen_addr, "server.crt", "server.key", nil)
	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
