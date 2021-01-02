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

import React, { useState } from "react";

import PropTypes from "prop-types";
import { connect } from "react-redux";
import { Modal } from "antd";
import Profile from "../../components/profile/profile";
import {
  uploadProfilePicAction,
  uploadVideoAction,
  changeVideoNameAction,
  convertVideoAction,
} from "../../actions/profileActions";
import responseTypes from "../../constants/responseTypes";
import { SUCCESS_MESSAGE, FAILURE_MESSAGE } from "../../constants/messages";

const ProfileContainer = (props) => {
  const {
    history,
    accessToken,
    videoId,
    uploadProfilePic,
    uploadVideo,
    changeVideoName,
    convertVideo,
  } = props;

  const [isVideoModalOpen, setIsVideoModalOpen] = useState(false);
  const [hasErrored, setHasErrored] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState("");

  const handleUploadProfilePic = (event) => {
    const callback = (res, data) => {
      if (res === responseTypes.SUCCESS) {
        Modal.success({
          title: SUCCESS_MESSAGE,
          content: data,
        });
      } else {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    uploadProfilePic({ callback, accessToken, file: event.target.files[0] });
  };

  const handleUploadVideo = (event) => {
    const callback = (res, data) => {
      if (res === responseTypes.SUCCESS) {
        Modal.success({
          title: SUCCESS_MESSAGE,
          content: data,
        });
      } else {
        Modal.error({
          title: FAILURE_MESSAGE,
          content: data,
        });
      }
    };
    uploadVideo({ callback, accessToken, file: event.target.files[0] });
  };

  const handleChangeVideoName = (values) => {
    const callback = (res, data) => {
      if (res === responseTypes.SUCCESS) {
        setIsVideoModalOpen(false);
        Modal.success({
          title: SUCCESS_MESSAGE,
          content: data,
        });
      } else {
        setHasErrored(true);
        setErrorMessage(data);
      }
    };
    changeVideoName({
      callback,
      accessToken,
      videoId,
      ...values,
    });
  };

  const shareVideoWithCommunity = () => {
    const callback = (res, data) => {
      Modal.error({
        title: FAILURE_MESSAGE,
        content: data,
      });
    };
    convertVideo({ callback, accessToken, videoId });
  };

  return (
    <Profile
      history={history}
      hasErrored={hasErrored}
      errorMessage={errorMessage}
      uploadProfilePic={handleUploadProfilePic}
      uploadVideo={handleUploadVideo}
      isVideoModalOpen={isVideoModalOpen}
      setIsVideoModalOpen={setIsVideoModalOpen}
      onVideoFormFinish={handleChangeVideoName}
      shareVideoWithCommunity={shareVideoWithCommunity}
    />
  );
};

const mapStateToProps = ({
  userReducer: { accessToken },
  profileReducer: { videoId },
}) => {
  return { accessToken, videoId };
};

const mapDispatchToProps = {
  uploadProfilePic: uploadProfilePicAction,
  uploadVideo: uploadVideoAction,
  changeVideoName: changeVideoNameAction,
  convertVideo: convertVideoAction,
};

ProfileContainer.propTypes = {
  accessToken: PropTypes.string,
  videoId: PropTypes.number,
  history: PropTypes.object,
  uploadProfilePic: PropTypes.func,
  uploadVideo: PropTypes.func,
  changeVideoName: PropTypes.func,
  convertVideo: PropTypes.func,
};

export default connect(mapStateToProps, mapDispatchToProps)(ProfileContainer);
