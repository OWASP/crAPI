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
import React from "react";
import { connect, ConnectedProps } from "react-redux";
import {
  Row,
  Col,
  Layout,
  Descriptions,
  Card,
  Button,
  Avatar,
  Typography,
} from "antd";
import { PageHeader } from "@ant-design/pro-components";
import { PlusOutlined } from "@ant-design/icons";
import { formatDateFromIso } from "../../utils";
import defaultProficPic from "../../assets/default_profile_pic.png";
import { useNavigate } from "react-router-dom";
import ReactionButtons, { Reaction } from "../reactions/reactionButtons";

const { Content } = Layout;
const { Meta } = Card;
const { Paragraph } = Typography;

interface Post {
  id: string;
  title: string;
  content: string;
  CreatedAt: string;
  author: {
    nickname: string;
    profile_pic_url: string | null;
  };
  likes: number;
  dislikes: number;
  user_reaction: string;
}

interface RootState {
  communityReducer: {
    posts: Post[];
    prevOffset: number | null;
    nextOffset: number | null;
  };
}

const mapStateToProps = (state: RootState) => ({
  posts: state.communityReducer.posts,
  prevOffset: state.communityReducer.prevOffset,
  nextOffset: state.communityReducer.nextOffset,
});

const connector = connect(mapStateToProps);

type PropsFromRedux = ConnectedProps<typeof connector>;

interface ForumProps extends PropsFromRedux {
  handleOffsetChange: (offset: number | null) => void;
  handleReaction: (postId: string, reaction: Reaction) => void;
}

const Forum: React.FC<ForumProps> = (props) => {
  const navigate = useNavigate();
  const { posts, prevOffset, nextOffset } = props;

  const renderAvatar = (url: string | null) => (
    <Avatar src={url || defaultProficPic} size="large" />
  );

  console.log("Prev offset", prevOffset);
  console.log("Next offset", nextOffset);

  const handleNewPostClick = () => {
    navigate("/new-post");
  };

  const handlePostClick = (postId: string) => {
    navigate(`/post?post_id=${postId}`);
  };

  return (
    <Layout className="page-container">
      <PageHeader
        title="Forum"
        className="page-header"
        extra={[
          <Button
            type="primary"
            shape="round"
            icon={<PlusOutlined />}
            size="large"
            key="add-coupons"
            onClick={handleNewPostClick}
          >
            New Post
          </Button>,
        ]}
      />
      <Content>
        <Row gutter={[32, 32]}>
          {posts.map((post) => (
            <Col span={24} key={post.id}>
              <Card hoverable onClick={() => handlePostClick(post.id)}>
                <Meta
                  avatar={renderAvatar(post.author.profile_pic_url)}
                  title={post.title}
                />
                <Descriptions size="small" column={1}>
                  <Descriptions.Item label="Posted by">
                    {post.author.nickname}
                  </Descriptions.Item>
                  <Descriptions.Item label="Posted on">
                    {formatDateFromIso(post.CreatedAt)}
                  </Descriptions.Item>
                </Descriptions>
                <Typography className="post-content">
                  {post.content.split("\n").map((para, index) => (
                    <Paragraph key={index}>{para}</Paragraph>
                  ))}
                </Typography>
                <ReactionButtons
                  likes={post.likes}
                  dislikes={post.dislikes}
                  userReaction={post.user_reaction}
                  onReact={(reaction) =>
                    props.handleReaction(post.id, reaction)
                  }
                />
              </Card>
            </Col>
          ))}
        </Row>
        <Row justify="center" className="pagination">
          <Col>
            <Button
              type="primary"
              shape="round"
              size="large"
              onClick={() => props.handleOffsetChange(prevOffset)}
              disabled={prevOffset === null}
            >
              ← Previous
            </Button>
          </Col>
          <Col>
            <Button
              type="primary"
              shape="round"
              size="large"
              onClick={() => props.handleOffsetChange(nextOffset)}
              disabled={!nextOffset}
            >
              Next →
            </Button>
          </Col>
        </Row>
      </Content>
    </Layout>
  );
};

export default connector(Forum);
