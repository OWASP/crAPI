package models

import (
	"context"
	"errors"
	"fmt"
	"html"
	"log"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/golang/protobuf/ptypes"

	"crapi.community/graphql.grpc/graph/model"
	pb "crapi.community/graphql.grpc/grpc/proto"

	"github.com/lithammer/shortuuid"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// func StringToTimestamp(str string) *timestamppb.Timestamp {
// 	ts, err := time.Parse(time.RFC3339, str)
// 	if err != nil {
// 		return nil
// 	}
// 	return timestamppb.New(ts)
// }

func PrepareNewPost(post model.Post) model.Post {
	post = model.Post{
		ID:        shortuuid.New(),
		Title:     html.EscapeString(strings.TrimSpace(post.Title)),
		Content:   html.EscapeString(strings.TrimSpace(post.Content)),
		Author:    PrepareUser(),
		Comments:  []*model.Comment{},
		Authorid:  strconv.FormatUint(autherID, 10),
		CreatedAt: time.Now(),
	}
	return post
}

func Convert_to_Time(time *timestamppb.Timestamp) time.Time {
	t, err := ptypes.Timestamp(time)
	if err != nil {
		// we consider this will never happen, since we always have the time.Time in post
	}
	return t
}
func PrepareUpdatePost(post model.PostInput) model.Post {
	// used pointer and reference to handle a warning
	p := &model.Post{
		ID:        shortuuid.New(),
		Title:     html.EscapeString(strings.TrimSpace(post.Title)),
		Content:   html.EscapeString(strings.TrimSpace(post.Content)),
		Author:    PrepareUser(),
		Comments:  []*model.Comment{},
		Authorid:  strconv.FormatUint(autherID, 10),
		CreatedAt: time.Now(),
	}
	return *p
}

func Convert_Graph_pb_post(post model.Post) *pb.Post {
	ListofModelComments := []*pb.Comment{}
	for i := 0; i < len(post.Comments); i++ {
		c := &pb.Comment{
			Id:        post.Comments[i].ID,
			Content:   post.Comments[i].Content,
			CreatedAt: timestamppb.New(post.Comments[i].CreatedAt),
			Author: &pb.User{
				// Id:        post.Comments[i].Author.ID,
				Nickname:  post.Comments[i].Author.Nickname,
				Email:     post.Comments[i].Author.Email,
				VehicleId: post.Comments[i].Author.Vehicleid,
				Picurl:    post.Comments[i].Author.ProfilePicURL,
				CreatedAt: timestamppb.New(post.Comments[i].Author.CreatedAt),
			},
		}
		ListofModelComments = append(ListofModelComments, c)
	}
	p := &pb.Post{
		Id:      post.ID,
		Title:   post.Title,
		Content: post.Content,
		Author: &pb.User{
			// Id:        post.Author.ID,
			Nickname:  post.Author.Nickname,
			Email:     post.Author.Email,
			VehicleId: post.Author.Vehicleid,
			Picurl:    post.Author.ProfilePicURL,
			CreatedAt: timestamppb.New(post.Author.CreatedAt),
		},
		AuthorId:  post.Authorid,
		Comments:  ListofModelComments,
		CreatedAt: timestamppb.New(post.CreatedAt),
	}
	return p
}

func Convert_pb_Graph_post(post *pb.Post) model.Post {
	ListofModelComments := []*model.Comment{}
	for i := 0; i < len(post.Comments); i++ {
		c := model.Comment{
			ID:        post.Comments[i].Id,
			Content:   post.Comments[i].Content,
			CreatedAt: Convert_to_Time(post.Comments[i].CreatedAt),
			Author: &model.User{
				// ID:        post.Comments[i].Author.GetId(),
				Nickname:      post.Comments[i].Author.GetNickname(),
				Email:         post.Comments[i].Author.GetEmail(),
				Vehicleid:     post.Comments[i].Author.GetVehicleId(),
				ProfilePicURL: post.Comments[i].Author.GetPicurl(),
				CreatedAt:     Convert_to_Time(post.Comments[i].Author.GetCreatedAt()),
			},
		}
		ListofModelComments = append(ListofModelComments, &c)
	}
	res := model.Post{
		ID:      post.Id,
		Title:   post.Title,
		Content: post.Content,
		Author: &model.User{
			// ID:        post.Author.GetId(),
			Nickname:      post.Author.GetNickname(),
			Email:         post.Author.GetEmail(),
			Vehicleid:     post.Author.GetVehicleId(),
			ProfilePicURL: post.Author.GetPicurl(),
			CreatedAt:     Convert_to_Time(post.Author.GetCreatedAt()),
		},
		Authorid:  post.GetAuthorId(),
		Comments:  ListofModelComments,
		CreatedAt: Convert_to_Time(post.GetCreatedAt()),
	}
	return res
}

func Validate(post *pb.CreatePostRequest) error {

	if post.Post.Title == "" {
		return errors.New("required title")
	}
	if post.Post.Content == "" {
		return errors.New("required content")
	}
	if len(post.Post.AuthorId) < 1 {
		return errors.New("required author")
	}
	return nil
}

// // Prepare initialize Field
// func PrepareAuth() Author {
// 	var u Author
// 	u.Nickname = nickname
// 	u.Email = userEmail
// 	u.VehicleID = vehicleID
// 	u.CreatedAt = time.Now()
// 	u.Picurl = picurl
// 	return u
// }

// SavePost persits data into database
func SavePost(client *mongo.Client, post *pb.Post) (*pb.CreatePostResponse, error) {

	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("post")
	_, err := collection.InsertOne(context.TODO(), post)
	if err != nil {
		println("Error while inserting post into collection")
		fmt.Println(err)
	}

	res := &pb.CreatePostResponse{
		Success: true,
	}
	return res, nil
}

// Get an array of all posts having matching id
func GetPosts(client *mongo.Client, in []string) (*pb.GetPostsResponse, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("post")
	var posts [](*pb.Post)
	for i := 0; i < len(in); i++ {
		filter := bson.D{{"id", in[i]}}
		var result *pb.Post
		err := collection.FindOne(context.TODO(), filter).Decode(&result)
		if err != nil {
			log.Println("Fetching documents from collection failed, %v", err)
		} else {
			posts = append(posts, result)
		}
	}
	res := &pb.GetPostsResponse{
		Posts: posts,
	}
	return res, nil
}

func GetAllPosts(client *mongo.Client, NoOfPosts int) ([]*pb.Post, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("post")
	var posts []*pb.Post
	filter := bson.D{{}}
	findOptions := options.Find()
	findOptions.SetSort(bson.D{{"_id", -1}})
	cur, err := collection.Find(context.TODO(), filter, findOptions)
	if err != nil {
		log.Println("Error while fetching all posts, %v", err)
		return nil, err
	}

	PostCounter := 0
	for cur.Next(context.TODO()) {
		var p *pb.Post
		err := cur.Decode(&p)
		if err != nil {
			log.Println("Can not decode the cursor into post, %v", err)
			return nil, err
		}
		p.CreatedAt = p.CreatedAt
		p.Author.CreatedAt = p.Author.CreatedAt
		posts = append(posts, p)
		PostCounter = PostCounter + 1
		if PostCounter >= NoOfPosts {
			break
		}
	}
	return posts, nil
}
