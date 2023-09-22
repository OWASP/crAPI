package graph

import (
	"context"
	"log"

	"crapi.community/graphql.grpc/graph/model"

	pb "crapi.community/graphql.grpc/grpc/proto"

	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func CreateCoupon(address string, cp model.Coupon) (bool, error) {
	conn, err := grpc.Dial(address, grpc.WithInsecure())
	if err != nil {
		log.Println("Cannot connect to server, %v", err)
		return false, err
	}
	defer conn.Close()

	client := pb.NewCommunityServiceClient(conn)

	ctx := context.Background()
	ctx = metadata.NewOutgoingContext(
		ctx,
		metadata.Pairs("key1", "val1", "key2", "val2"),
	)

	created, err := client.CreateCoupon(ctx, &pb.CreateCouponRequest{
		Coupon: &pb.Coupon{
			CouponCode: cp.CouponCode,
			Amount:     cp.Amount,
			CreatedAt:  timestamppb.New(cp.CreatedAt),
		}})

	if err != nil {
		log.Println("Failed creating a Coupon, %v", err)
		return false, err
	}
	return created.GetSuccess(), nil
}

func GetCoupon(address string, ids []string) *pb.GetCouponsResponse {
	conn, err := grpc.Dial(address, grpc.WithInsecure())
	if err != nil {
		log.Println("Cannot connect to server, %v", err)
		return &pb.GetCouponsResponse{}
	}
	defer conn.Close()

	client := pb.NewCommunityServiceClient(conn)

	ctx := context.Background()
	ctx = metadata.NewOutgoingContext(
		ctx,
		metadata.Pairs("key1", "val1", "key2", "val2"),
	)

	GetCoupon, err := client.GetCoupons(ctx, &pb.GetCouponsRequest{
		CouponCodes: ids,
	})

	if err != nil {
		log.Println("Get Coupons failed, %v", err)
		return nil
	}
	return &pb.GetCouponsResponse{
		Coupons: GetCoupon.Coupons,
	}
}
