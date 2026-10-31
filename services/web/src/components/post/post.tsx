/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import "./style.css";
import React, { Fragment } from "react";
import Linkify from "react-linkify";
import { connect, ConnectedProps } from "react-redux";
import {
  Layout,
  Avatar,
  Row,
  Col,
  Card,
  Button,
  Typography,
  Divider,
  Form,
  Input,
  Modal,
} from "antd";
import { PageHeader } from "@ant-design/pro-components";
import { CommentOutlined } from "@ant-design/icons";
import { COMMENT_REQUIRED } from "../../constants/messages";
import { formatDateFromIso } from "../../utils";
import defaultProficPic from "../../assets/default_profile_pic.png";
import ReactionButtons, { Reaction } from "../reactions/reactionButtons";

const { Content } = Layout;
const { Paragraph } = Typography;

interface Author {
  nickname: string;
  profile_pic_url: string;
}

interface Comment {
  CreatedAt: string;
  author: Author;
  content: string;
}

interface Post {
  title: string;
  author: Author;
  CreatedAt: string;
  content: string;
  comments: Comment[];
  likes: number;
  dislikes: number;
  user_reaction: string;
}

interface RootState {
  communityReducer: {
    post: Post;
  };
}

interface PostProps extends PropsFromRedux {
  onFinish: (values: any) => void;
  onReact: (reaction: Reaction) => void;
  isCommentFormOpen: boolean;
  setIsCommentFormOpen: (isOpen: boolean) => void;
  hasErrored: boolean;
  errorMessage: string;
}

const Posts: React.FC<PostProps> = ({
  onFinish,
  onReact,
  post,
  isCommentFormOpen,
  setIsCommentFormOpen,
  hasErrored,
  errorMessage,
}) => {
  return (
    <Layout className="page-container post-page">
      <Card className="post-card" hoverable>
        <div className="post-header-section">
          <Row gutter={[24, 24]} align="middle">
            <Col>
              <div className="post-avatar-container">
                <Avatar
                  src={
                    (post && post.author.profile_pic_url) || defaultProficPic
                  }
                  size={90}
                  className="post-avatar"
                />
              </div>
            </Col>
            <Col flex="auto">
              <div className="post-meta">
                <h1 className="post-title">{post && post.title}</h1>
                <div className="post-subtitle">
                  {post &&
                    `${post.author.nickname}, ${formatDateFromIso(post.CreatedAt)}`}
                </div>
                {post && (
                  <div className="post-reactions">
                    <ReactionButtons
                      likes={post.likes}
                      dislikes={post.dislikes}
                      userReaction={post.user_reaction}
                      onReact={onReact}
                      size="large"
                    />
                  </div>
                )}
              </div>
            </Col>
          </Row>
        </div>

        <div className="post-content-section">
          <Typography className="post-content">
            {post &&
              post.content.split("\n").map((para, index) => (
                <Paragraph key={index} className="post-paragraph">
                  <Linkify>{para}</Linkify>
                </Paragraph>
              ))}
          </Typography>
        </div>

        <Divider className="post-divider" />

        <div className="comments-section">
          <div className="comments-header">
            <h2 className="comments-title">Comments</h2>
            <Button
              type="primary"
              shape="round"
              icon={<CommentOutlined />}
              size="large"
              className="add-comment-btn"
              onClick={() => setIsCommentFormOpen(true)}
            >
              Add Comment
            </Button>
          </div>

          <div className="comments-list">
            {post &&
              post.comments &&
              post.comments.map((comment, index) => (
                <div
                  key={`${comment.CreatedAt}-${index}`}
                  className="comment-item"
                >
                  <Row gutter={[16, 16]} align="top">
                    <Col>
                      <div className="comment-avatar-container">
                        <Avatar
                          src={
                            comment.author.profile_pic_url || defaultProficPic
                          }
                          size={50}
                          className="comment-avatar"
                        />
                      </div>
                    </Col>
                    <Col flex="auto">
                      <div className="comment-content">
                        <div className="comment-meta">
                          {`${comment.author.nickname}, ${formatDateFromIso(
                            comment.CreatedAt,
                          )}`}
                        </div>
                        <Typography className="comment-text">
                          {comment.content
                            .split("\n")
                            .map((para, paraIndex) => (
                              <Paragraph
                                key={paraIndex}
                                className="comment-paragraph"
                              >
                                {para}
                              </Paragraph>
                            ))}
                        </Typography>
                      </div>
                    </Col>
                  </Row>
                </div>
              ))}
          </div>
        </div>
      </Card>
      <Modal
        title={<span className="modal-title">💬 New Comment</span>}
        open={isCommentFormOpen}
        footer={null}
        onCancel={() => setIsCommentFormOpen(false)}
        className="comment-modal"
        centered
      >
        <Form
          name="comment-form"
          layout="vertical"
          initialValues={{
            remember: true,
          }}
          onFinish={onFinish}
          className="comment-form"
        >
          <Form.Item
            name="comment"
            label="Your Comment"
            rules={[{ required: true, message: COMMENT_REQUIRED }]}
          >
            <Input.TextArea
              rows={4}
              placeholder="Share your thoughts..."
              className="comment-textarea"
            />
          </Form.Item>
          <Form.Item className="form-actions">
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            <div className="modal-buttons">
              <Button
                onClick={() => setIsCommentFormOpen(false)}
                className="cancel-btn"
              >
                Cancel
              </Button>
              <Button
                type="primary"
                htmlType="submit"
                className="submit-btn"
                icon={<CommentOutlined />}
              >
                Add Comment
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

const mapStateToProps = (state: RootState) => ({
  post: state.communityReducer.post,
});

const connector = connect(mapStateToProps);

type PropsFromRedux = ConnectedProps<typeof connector>;

export default connector(Posts);
