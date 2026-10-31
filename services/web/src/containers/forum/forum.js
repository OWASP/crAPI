/*
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

import React, { useEffect } from "react";
import { Modal } from "antd";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import {
  getPostsAction,
  reactToPostAction,
} from "../../actions/communityActions";
import Forum from "../../components/forum/forum";
import responseTypes from "../../constants/responseTypes";
import { FAILURE_MESSAGE } from "../../constants/messages";

const ForumContainer = (props) => {
  const { accessToken, getPosts, reactToPost } = props;

  useEffect(() => {
    const callback = (res, data) => {
      if (res !== responseTypes.SUCCESS) {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    getPosts({ accessToken, callback });
  }, [accessToken, getPosts]);

  const onOffsetChange = (offset) => {
    const callback = (res, data) => {
      if (res !== responseTypes.SUCCESS) {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    getPosts({ accessToken, offset, callback });
  };

  const onReaction = (postId, reaction) => {
    const callback = (res, data) => {
      if (res !== responseTypes.SUCCESS) {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    reactToPost({ accessToken, postId, reaction, callback });
  };

  return (
    <Forum handleOffsetChange={onOffsetChange} handleReaction={onReaction} />
  );
};

const mapStateToProps = ({ userReducer: { accessToken } }) => {
  return { accessToken };
};

const mapDispatchToProps = {
  getPosts: getPostsAction,
  reactToPost: reactToPostAction,
};

ForumContainer.propTypes = {
  accessToken: PropTypes.string,
  getPosts: PropTypes.func,
  reactToPost: PropTypes.func,
  prevOffset: PropTypes.string,
  nextOffset: PropTypes.string,
  handleOffsetChange: PropTypes.func,
};

export default connect(mapStateToProps, mapDispatchToProps)(ForumContainer);
