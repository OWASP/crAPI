package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"hash/fnv"
	"log"
	"math/rand"
	"net/http"
	"strings"
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

type User struct {
	Name  string `json:"name"`
	Email string `json:"email"`
	Phone string `json:"number"`
}

type Order struct {
	TransactionId string `json:"transaction_id"`
	OrderId       int64  `json:"id"`
	CreatedOn     string `json:"created_on"`
}

type PaymentInfoRequest struct {
	Order  Order   `json:"order"`
	User   User    `json:"user"`
	Amount float64 `json:"amount"`
}

type PaymentInfoResponse struct {
	TransactionId string  `json:"transaction_id"`
	OrderId       int64   `json:"order_id"`
	Amount        float64 `json:"amount"`
	PaidOn        string  `json:"paid_on"`
	CardNumber    string  `json:"card_number"`
	CardOwnerName string  `json:"card_owner_name"`
	CardType      string  `json:"card_type"`
	CardExpiry    string  `json:"card_expiry"`
	Currency      string  `json:"currency"`
}

func HelloServer(w http.ResponseWriter, req *http.Request) {
	w.Header().Set("Content-Type", "text/plain")
	w.Write([]byte("crAPI Gateway.\n"))
}

func GetOwners(w http.ResponseWriter, r *http.Request) {
	user, pass, ok := r.BasicAuth()
	if !ok {
		http.Error(w, "Bad Request. Invalid Auth.", 400)
		return
	}
	if !checkCreds(user, pass) {
		http.Error(w, "Unauthorized.", 401)
		return
	}
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

func GetPayMentInfo(w http.ResponseWriter, r *http.Request) {
	user, pass, ok := r.BasicAuth()
	if !ok {
		http.Error(w, "Bad Request. Invalid Auth.", 400)
		log.Printf("Bad Request. Invalid Auth. %s\n", r.Header["Authorization"][0])
		return
	}
	if !checkCreds(user, pass) {
		http.Error(w, "Unauthorized.", 401)
		return
	}
	var p_req PaymentInfoRequest
	err := json.NewDecoder(r.Body).Decode(&p_req)
	if err != nil {
		http.Error(w, fmt.Sprintf("Bad Request. Invalid Body %s.", err.Error()), 400)
		log.Printf("Bad Request. Invalid Body %s\n", err.Error())
		return
	}
	h := fnv.New32a()
	h.Write([]byte(p_req.User.Phone))
	seed := int64(h.Sum32())
	src := rand.NewSource(seed)
	fake := faker.NewWithSeed(src)
	payment_res := PaymentInfoResponse{}
	payment_res.TransactionId = p_req.Order.TransactionId
	payment_res.OrderId = p_req.Order.OrderId
	payment_res.PaidOn = p_req.Order.CreatedOn
	payment_card := fake.Payment()
	payment_res.CardExpiry = fmt.Sprintf("%02d/%04d", fake.IntBetween(1, 12), fake.IntBetween(time.Now().Year()+1, 2030))
	payment_res.CardNumber = maskLeft(payment_card.CreditCardNumber())
	payment_res.CardOwnerName = strings.ReplaceAll(p_req.User.Name, ".", " ")
	payment_res.CardType = payment_card.CreditCardType()
	payment_res.Amount = p_req.Amount
	payment_res.Currency = "USD"
	response_body, err := json.Marshal(payment_res)
	if err != nil {
		http.Error(w, fmt.Sprintf("Bad Request. Invalid Body %s", err.Error()), 400)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.Write(response_body)
}

func checkCreds(user string, pass string) bool {
	if user == "vendorcrapi" && pass == "Pa$$4Vendor_1" {
		return true
	}
	return false
}

func maskLeft(s string) string {
	rs := []rune(s)
	for i := 0; i < len(rs)-4; i++ {
		rs[i] = 'X'
	}
	return string(rs)
}

func main() {
	port := flag.Int("port", 443, "port number")
	flag.Parse()
	http.HandleFunc("/", HelloServer)
	http.HandleFunc("/v1/vin/ownership", GetOwners)
	http.HandleFunc("/v1/payment", GetPayMentInfo)
	listen_addr := fmt.Sprintf(":%d", *port)
	log.Printf("Listening on %s", listen_addr)
	err := http.ListenAndServeTLS(listen_addr, "server.crt", "server.key", nil)
	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
