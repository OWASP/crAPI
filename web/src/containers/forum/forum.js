import React, { useEffect } from "react";
import { Modal } from "antd";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import { getPostsAction } from "../../actions/communityActions";
import Forum from "../../components/forum/forum";
import responseTypes from "../../constants/responseTypes";
import { FAILURE_MESSAGE } from "../../constants/messages";

const ForumContainer = (props) => {
  const { history, accessToken, getPosts } = props;

  useEffect(() => {
    const callback = (res, data) => {
      if (res !== responseTypes.SUCCESS) {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    getPosts({ callback, accessToken });
  }, [accessToken, getPosts]);

  return <Forum history={history} />;
};

const mapStateToProps = ({ userReducer: { accessToken } }) => {
  return { accessToken };
};

const mapDispatchToProps = {
  getPosts: getPostsAction,
};

ForumContainer.propTypes = {
  accessToken: PropTypes.string,
  getPosts: PropTypes.func,
  history: PropTypes.object,
};

export default connect(mapStateToProps, mapDispatchToProps)(ForumContainer);
