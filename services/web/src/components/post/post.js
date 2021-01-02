/*
 * Copyright 2020 Traceable, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import "./style.css";
import React, { Fragment } from "react";
import Linkify from "react-linkify";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import {
  Layout,
  PageHeader,
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
import { CommentOutlined } from "@ant-design/icons";
import { COMMENT_REQUIRED } from "../../constants/messages";
import { formatDateFromIso } from "../../utils";
import defaultProficPic from "../../assets/default_profile_pic.png";

const { Content } = Layout;
const { Paragraph } = Typography;

const Post = (props) => {
  const {
    onFinish,
    post,
    isCommentFormOpen,
    setIsCommentFormOpen,
    hasErrored,
    errorMessage,
  } = props;

  return (
    <Layout className="page-container">
      <Card>
        <Row gutter={[20, 20]}>
          <Col flex="75px">
            <Avatar
              src={defaultProficPic || (post && post.author.profile_pic_url)}
              size={75}
            />
          </Col>
          <Col flex="auto">
            <PageHeader
              title={post && post.title}
              className="page-header post-header"
            />
            <PageHeader
              subTitle={
                post &&
                `${post.author.nickname}, ${formatDateFromIso(post.CreatedAt)}`
              }
              className="page-header post-subtitle"
            />
          </Col>
        </Row>
        <Typography>
          {post &&
            post.content.split("\n").map((para) => (
              <Paragraph key={para}>
                <Linkify>{para}</Linkify>
              </Paragraph>
            ))}
        </Typography>
        <Divider />
        <PageHeader
          title="Comments"
          className="page-header comment-heading"
          extra={[
            <Button
              type="primary"
              shape="round"
              icon={<CommentOutlined />}
              size="large"
              key="add-btn"
              onClick={() => setIsCommentFormOpen(true)}
            >
              Add Comment
            </Button>,
          ]}
        />
        {post &&
          post.comments &&
          post.comments.map((comment) => (
            <Fragment key={comment.CreatedAt}>
              <Row gutter={[20, 20]} className="comment-row">
                <Col>
                  <Avatar
                    src={defaultProficPic || comment.author.profile_pic_url}
                    size="large"
                  />
                </Col>
                <Col flex="auto">
                  <PageHeader
                    subTitle={`${comment.author.nickname}, ${formatDateFromIso(
                      comment.CreatedAt
                    )}`}
                    className="page-header comment-title"
                  />
                </Col>
              </Row>
              <Typography>
                {comment.content.split("\n").map((para) => (
                  <Paragraph key={para}>{para}</Paragraph>
                ))}
              </Typography>
              <Divider />
            </Fragment>
          ))}
        <Content />
      </Card>
      <Modal
        title="New Comment"
        visible={isCommentFormOpen}
        footer={null}
        onCancel={() => setIsCommentFormOpen(false)}
      >
        <Form
          name="basic"
          initialValues={{
            remember: true,
          }}
          onFinish={onFinish}
        >
          <Form.Item
            name="comment"
            rules={[{ required: true, message: COMMENT_REQUIRED }]}
          >
            <Input.TextArea />
          </Form.Item>
          <Form.Item>
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            <Button type="primary" htmlType="submit" className="form-button">
              Add a Comment
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

Post.propTypes = {
  post: PropTypes.object,
  onFinish: PropTypes.func,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
  isCommentFormOpen: PropTypes.bool,
  setIsCommentFormOpen: PropTypes.func,
};

const mapStateToProps = ({ communityReducer: { post } }) => {
  return { post };
};

export default connect(mapStateToProps)(Post);
