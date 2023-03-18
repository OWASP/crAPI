package main

import (
	"context"
	"log"
	"net"
	"os"

	"crapi.community/graphql.grpc/graphql/config"
	"crapi.community/graphql.grpc/graphql/model"
	"crapi.community/graphql.grpc/graphql/seed"
	"crapi.community/graphql.grpc/grpc/models"

	pb "crapi.community/graphql.grpc/grpc/proto"

	"go.mongodb.org/mongo-driver/mongo"
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

func (s *server) UpdatePost(ctx context.Context, in *pb.UpdatePostRequest) (*pb.UpdatePostResponse, error) {
	id := in.GetId()

	_, err := models.UpdatePost(mongoClient, in.GetUpdatedPost(), id)

	if err != nil {
		log.Println("Could not update the data in DB")
		return nil, err
	}
	res := &pb.UpdatePostResponse{
		Success: true,
	}
	return res, nil
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

func (s *server) DeletePosts(ctx context.Context, in *pb.DeletePostsRequest) (*pb.DeletePostsResponse, error) {
	ids := in.Ids
	for i := 0; i < len(ids); i++ {
		print(ids[i], " ")
	}

	DeletePosts, err := models.DeletePosts(mongoClient, ids)
	if err != nil {
		log.Println("DeletePosts failed!! %v", err)
		return nil, err
	}
	return DeletePosts, err
}

func (s *server) CreateUser(ctx context.Context, in *pb.CreateUserRequest) (*pb.CreateUserResponse, error) {
	user := in.GetUser()

	_, err := models.SaveUser(mongoClient, user)
	if err != nil {
		log.Println("Can not save user to mysql, %v", err)
		return nil, err
	}
	response := &pb.CreateUserResponse{
		Success: true,
	}
	return response, nil
}

func (s *server) UpdateUser(ctx context.Context, in *pb.UpdateUserRequest) (*pb.UpdateUserResponse, error) {
	id := in.GetId()
	_, err := models.UpdateUser(mongoClient, in.GetUpdatedUser(), id)

	if err != nil {
		log.Println("Could not update the user in DB")
		return nil, err
	}
	res := &pb.UpdateUserResponse{
		Success: true,
	}
	return res, nil
}

func (s *server) GetUsers(ctx context.Context, in *pb.GetUsersRequest) (*pb.GetUsersResponse, error) {
	ids := in.Ids
	getUsers, err := models.GetUsers(mongoClient, ids)
	if err != nil {
		log.Println("GetUsers failed!! %v", err)
		return nil, err
	}
	return getUsers, err
}

func (s *server) DeleteUsers(ctx context.Context, in *pb.DeleteUsersRequest) (*pb.DeleteUsersResponse, error) {
	ids := in.Ids

	DeleteUsers, err := models.DeleteUsers(mongoClient, ids)
	if err != nil {
		log.Println("DeleteUsers failed!! %v", err)
		return nil, err
	}
	return DeleteUsers, err
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

func (s *server) UpdateCoupon(ctx context.Context, in *pb.UpdateCouponRequest) (*pb.UpdateCouponResponse, error) {
	couponcode := in.GetCouponCode()

	_, err := models.UpdateCoupon(mongoClient, in.GetUpdatedCoupon(), couponcode)

	if err != nil {
		log.Println("Could not update the coupon in DB")
		return nil, err
	}

	res := &pb.UpdateCouponResponse{
		Success: true,
	}
	return res, nil
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

func (s *server) DeleteCoupons(ctx context.Context, in *pb.DeleteCouponsRequest) (*pb.DeleteCouponsResponse, error) {
	couponcodes := in.CouponCodes

	DeleteCoupons, err := models.DeleteCoupons(mongoClient, couponcodes)
	if err != nil {
		log.Println("DeleteCoupons failed!! %v", err)
		return nil, err
	}
	return DeleteCoupons, err
}

func ValidateCoupon(client *mongo.Client, couponCode string) (*model.Coupon, error) {
	res, err := models.ValidateCoupon(client, couponCode)
	return res, err
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

// Not implemented the right way! Since its not used in application, so, will do it later when i have bandwidth
func (s *server) UpdateComment(ctx context.Context, in *pb.UpdateCommentRequest) (*pb.UpdateCommentResponse, error) {
	id := in.GetId()

	_, err := models.UpdateComment(mongoClient, in.GetUpdatedComment(), id)

	if err != nil {
		log.Println("Could not update the comment in DB")
		return nil, err
	}
	res := &pb.UpdateCommentResponse{
		Success: true,
	}
	return res, nil
}

// Not implemented the right way! Since its not used in application, so, will do it later when i have bandwidth
func (s *server) GetComments(ctx context.Context, in *pb.GetCommentsRequest) (*pb.GetCommentsResponse, error) {
	ids := in.Ids

	getComment, err := models.GetComments(mongoClient, ids)
	if err != nil {
		log.Println("GetComment failed!! %v", err)
		return nil, err
	}
	return getComment, err
}

// Not implemented the right way! Since its not used in application, so, will do it later when i have bandwidth
func (s *server) DeleteComments(ctx context.Context, in *pb.DeleteCommentsRequest) (*pb.DeleteCommentsResponse, error) {
	ids := in.Ids

	deleteComment, err := models.DeleteComments(mongoClient, ids)
	if err != nil {
		log.Println("DeleteComment failed!! %v", err)
		return nil, err
	}
	return deleteComment, err
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
