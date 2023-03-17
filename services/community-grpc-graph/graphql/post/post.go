package graph

import (
	"context"
	"log"

	"crapi.community/graphql.grpc/grpc/models"

	"crapi.community/graphql.grpc/graphql/model"
	pb "crapi.community/graphql.grpc/grpc/proto"

	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// This is a function to create GetPostsResponse(gRPC data type) data type to model.Post(graphql data type)
func ConvertPost(p *pb.GetPostsResponse) []*model.Post {
	post := []*model.Post{}
	pb_posts := p.GetPosts()
	for i := 0; i < len(pb_posts); i++ {
		ListofModelComments := []*model.Comment{}
		for j := 0; j < len(pb_posts[i].GetComments()); j++ {
			c := model.Comment{
				ID:        pb_posts[i].GetComments()[j].GetId(),
				Content:   pb_posts[i].GetComments()[j].GetContent(),
				CreatedAt: models.Convert_to_Time(pb_posts[i].GetComments()[j].GetCreatedAt()),
				Author: &model.User{
					Nickname:      pb_posts[i].GetComments()[j].GetAuthor().GetNickname(),
					Email:         pb_posts[i].GetComments()[j].GetAuthor().GetEmail(),
					Vehicleid:     pb_posts[i].GetComments()[j].GetAuthor().GetVehicleId(),
					ProfilePicURL: pb_posts[i].GetComments()[j].GetAuthor().GetPicurl(),
					CreatedAt:     models.Convert_to_Time(pb_posts[i].GetComments()[j].GetCreatedAt()),
				},
			}
			ListofModelComments = append(ListofModelComments, &c)
		}
		model_post := &model.Post{
			ID:      pb_posts[i].GetId(),
			Title:   pb_posts[i].GetTitle(),
			Content: pb_posts[i].GetContent(),
			Author: &model.User{
				Nickname:      pb_posts[i].GetAuthor().GetNickname(),
				Email:         pb_posts[i].GetAuthor().GetEmail(),
				Vehicleid:     pb_posts[i].GetAuthor().GetVehicleId(),
				ProfilePicURL: pb_posts[i].GetAuthor().GetPicurl(),
				CreatedAt:     models.Convert_to_Time(pb_posts[i].GetAuthor().GetCreatedAt()),
			},
			Comments:  ListofModelComments,
			CreatedAt: models.Convert_to_Time(pb_posts[i].GetCreatedAt()),
		}
		post = append(post, model_post)
	}
	return post
}

func CreatePost(address string, p model.Post) (*pb.Post, error) {
	conn, err := grpc.Dial(address, grpc.WithInsecure())
	if err != nil {
		log.Println("Cannot connect to server, %v", err)
		return nil, err
	}
	defer conn.Close()

	client := pb.NewCommunityServiceClient(conn)

	ctx := context.Background()
	ctx = metadata.NewOutgoingContext(
		ctx,
		metadata.Pairs("key1", "val1", "key2", "val2"),
	)

	ListofModelComments := []*pb.Comment{}
	post := &pb.Post{
		Id:      p.ID,
		Title:   p.Title,
		Content: p.Content,
		Author: &pb.User{
			// Id:        p.Author.ID,
			Nickname:  p.Author.Nickname,
			Email:     p.Author.Email,
			VehicleId: p.Author.Vehicleid,
			Picurl:    p.Author.ProfilePicURL,
			CreatedAt: timestamppb.New(p.Author.CreatedAt),
		},
		Comments:  ListofModelComments,
		CreatedAt: timestamppb.New(p.CreatedAt),
	}
	_, err = client.CreatePost(ctx, &pb.CreatePostRequest{
		Post: post,
	})

	if err != nil {
		log.Println("Failed creating a post, %v", err)
		return nil, err
	}
	return post, nil
}

func GetPosts(address string, ids []string) *pb.GetPostsResponse {
	conn, err := grpc.Dial(address, grpc.WithInsecure())
	if err != nil {
		log.Println("Cannot connect to server, %v", err)
		return &pb.GetPostsResponse{}
	}
	defer conn.Close()

	client := pb.NewCommunityServiceClient(conn)

	ctx := context.Background()
	ctx = metadata.NewOutgoingContext(
		ctx,
		metadata.Pairs("key1", "val1", "key2", "val2"),
	)

	GetPosts, err := client.GetPosts(ctx, &pb.GetPostsRequest{
		Ids: ids,
	})

	if err != nil {
		log.Println("GetPost failed")
		return nil
	}
	return &pb.GetPostsResponse{
		Posts: GetPosts.Posts,
	}
}
