// /*
//  * Licensed under the Apache License, Version 2.0 (the “License”);
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *         http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an “AS IS” BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */

package models

import (
	"context"
	"fmt"
	"log"
	"os"

	pb "crapi.community/graphql.grpc/grpc/proto"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func SaveComment(client *mongo.Client, comment *pb.Comment) (*pb.CreateCommentResponse, error) {

	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("comment")
	_, err := collection.InsertOne(context.TODO(), comment)
	if err != nil {
		println("Error while saving comment into collection")
		fmt.Println(err)
	}

	res := &pb.CreateCommentResponse{
		Success: true,
	}
	return res, nil
}

func UpdateComment(client *mongo.Client, comment *pb.Comment, id string) (*pb.UpdateCommentResponse, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("comment")

	opts := options.Update().SetUpsert(true)
	filter := bson.D{{"id", id}}
	update := bson.D{{"$set", comment}}

	_, err := collection.UpdateOne(context.TODO(), filter, update, opts)
	if err != nil {
		println("Error while updating comment by id")
		fmt.Println(err)
	}

	res := &pb.UpdateCommentResponse{
		Success: true,
	}
	return res, nil
}

func GetComments(client *mongo.Client, in []string) (*pb.GetCommentsResponse, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("comment")
	var comments [](*pb.Comment)
	for i := 0; i < len(in); i++ {
		filter := bson.D{{"id", in[i]}}
		var result *pb.Comment
		err := collection.FindOne(context.TODO(), filter).Decode(&result)
		if err != nil {
			log.Println("Fetching documents from collection failed, %v", err)
			return nil, err
		} else {
			comments = append(comments, result)
		}
	}
	res := &pb.GetCommentsResponse{
		Comments: comments,
	}
	return res, nil
}

func DeleteComments(client *mongo.Client, in []string) (*pb.DeleteCommentsResponse, error) {
	collection := client.Database(os.Getenv("MONGO_DB_NAME")).Collection("comment")
	var comments [](*pb.Comment)
	for i := 0; i < len(in); i++ {
		filter := bson.D{{"id", in[i]}}
		var comment *pb.Comment
		err_get := collection.FindOne(context.TODO(), filter).Decode(&comment)
		if err_get != nil {
			log.Println("Failed to get the comment from collection")
		}
		_, err := collection.DeleteOne(context.TODO(), filter)
		if err != nil {
			log.Println("Fetching documents from collection failed, %v", err)
			return nil, err
		} else {
			comments = append(comments, comment)
		}
	}
	res := &pb.DeleteCommentsResponse{
		DeletedComments: comments,
	}
	return res, nil
}
