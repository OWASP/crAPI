package main

import (
	"context"
	"log"
	"net"
	"os"

	"crapi.community/graphql.grpc/graph/config"
	"crapi.community/graphql.grpc/grpc/models"
	"crapi.community/graphql.grpc/grpc/seed"

	pb "crapi.community/graphql.grpc/grpc/proto"

	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
)

var serv = config.Server{}

type server struct {
	pb.UnimplementedCommunityServiceServer
}

var mongoClient = serv.Client
var DB = serv.DB

func (s *server) CreatePost(ctx context.Context, in *pb.CreatePostRequest) (*pb.CreatePostResponse, error) {
	post := in.GetPost()

	_, err := models.SavePost(mongoClient, post)
	if err != nil {
		log.Println("Can not save file to mysql, %v", err)
		return nil, err
	}

	response := &pb.CreatePostResponse{
		Success: true,
	}
	return response, nil
}

func (s *server) GetPosts(ctx context.Context, in *pb.GetPostsRequest) (*pb.GetPostsResponse, error) {
	ids := in.Ids

	getPosts, err := models.GetPosts(mongoClient, ids)
	if err != nil {
		log.Println("GetPosts failed!! %v", err)
		return nil, err
	} else {
		println("GetPost successful :) ")
	}
	return getPosts, err
}

func (s *server) GetAllPosts(ctx context.Context, in *pb.GetAllPostRequest) (*pb.GetPostsResponse, error) {
	pbPost, err := models.GetAllPosts(mongoClient, int(in.NoOfPosts)) // converted the data type from int32 to int for simplifications

	if err != nil {
		log.Println("Error at grpc_server while fetching all the posts, %v", err)
		return nil, err
	}
	return &pb.GetPostsResponse{
		Posts: pbPost,
	}, nil
}

func (s *server) CreateCoupon(ctx context.Context, in *pb.CreateCouponRequest) (*pb.CreateCouponResponse, error) {
	coupon := in.GetCoupon()

	_, err := models.SaveCoupon(mongoClient, coupon)
	if err != nil {
		log.Println("Can not save coupon to mysql, %v", err)
		return nil, err
	}
	response := &pb.CreateCouponResponse{
		Success: true,
	}
	return response, nil
}

func (s *server) GetCoupons(ctx context.Context, in *pb.GetCouponsRequest) (*pb.GetCouponsResponse, error) {
	couponcodes := in.CouponCodes

	getCoupons, err := models.GetCoupons(mongoClient, couponcodes)
	if err != nil {
		log.Println("GetCoupons failed!! %v", err)
		return nil, err
	}

	return getCoupons, err
}

func (s *server) CreateComment(ctx context.Context, in *pb.CreateCommentRequest) (*pb.CreateCommentResponse, error) {
	comment := in.GetComment()
	id := []string{comment.GetId()}
	getPost, err := models.GetPosts(mongoClient, id)
	if err != nil {
		log.Println("Cannot find post with id %s", in.GetComment().GetId())
		return nil, err
	} else {
		up := getPost.GetPosts()[0]
		up.Comments = append(up.Comments, in.GetComment())
		_, err := models.UpdatePost(mongoClient, up, id[0])

		if err != nil {
			log.Println("Error while adding comment to post, %v", err)
			return nil, err
		}
	}
	return &pb.CreateCommentResponse{
		Success: true,
	}, nil
}

func main() {
	lis, err := net.Listen("tcp", ":9090")
	if err != nil {
		log.Printf("Fatal error! Did not connect %v", err)
	}
	log.Printf("Worked! Server listening at %v", lis.Addr())

	mongoClient = serv.InitializeMongo("mongodb", os.Getenv("MONGO_DB_USER"), os.Getenv("MONGO_DB_PASSWORD"), os.Getenv("MONGO_DB_PORT"), os.Getenv("MONGO_DB_HOST"))
	DB = serv.Initialize("postgres", os.Getenv("DB_USER"), os.Getenv("DB_PASSWORD"), os.Getenv("DB_PORT"), os.Getenv("DB_HOST"), os.Getenv("DB_NAME"))
	serv.Client = mongoClient
	serv.DB = DB

	println("Seeding the values in database")
	seed.LoadMongoData(mongoClient, serv.DB)

	grpcServer := grpc.NewServer()

	pb.RegisterCommunityServiceServer(grpcServer, &server{})
	reflection.Register(grpcServer)

	log.Println("Server registered")
	err = grpcServer.Serve(lis)
	if err != nil {
		log.Printf("Cannot start server")
	}

	log.Printf("Wohoo! sERVER started too! serving at %v", grpcServer.GetServiceInfo())
}
