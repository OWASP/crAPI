import React from "react";
import PropTypes from "prop-types";
import { Button, Form, Card, Input } from "antd";
import {
  POST_TITLE_REQUIRED,
  POST_DESC_REQUIRED,
} from "../../constants/messages";

const NewPost = (props) => {
  const urlParams = new URLSearchParams(window.location.search);
  const postContent = urlParams.get("content");
  const { hasErrored, errorMessage, onFinish } = props;
  return (
    <div className="container">
      <Card title="New Post" bordered={false} className="form-card">
        <Form
          name="new-post"
          initialValues={{
            remember: true,
          }}
          labelCol={{ sm: { span: 8 } }}
          wrapperCol={{ sm: { span: 16 } }}
          onFinish={onFinish}
        >
          <Form.Item
            name="title"
            label="Title"
            rules={[{ required: true, message: POST_TITLE_REQUIRED }]}
          >
            <Input placeholder="Post Title" />
          </Form.Item>
          <Form.Item
            name="content"
            label="Description"
            initialValue={postContent}
            rules={[{ required: true, message: POST_DESC_REQUIRED }]}
          >
            <Input.TextArea />
          </Form.Item>
          <Form.Item wrapperCol={{ sm: { span: 24 } }}>
            {hasErrored && <div className="error-message">{errorMessage}</div>}
            <Button type="primary" htmlType="submit" className="form-button">
              Add New Post
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

NewPost.propTypes = {
  onFinish: PropTypes.func,
  hasErrored: PropTypes.bool,
  errorMessage: PropTypes.string,
};

export default NewPost;
