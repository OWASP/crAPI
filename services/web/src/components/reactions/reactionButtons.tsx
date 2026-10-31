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
import { Button, Space } from "antd";
import {
  LikeOutlined,
  LikeFilled,
  DislikeOutlined,
  DislikeFilled,
} from "@ant-design/icons";

export type Reaction = "like" | "dislike";

interface ReactionButtonsProps {
  likes: number;
  dislikes: number;
  userReaction: string;
  onReact: (reaction: Reaction) => void;
  size?: "small" | "middle" | "large";
}

/**
 * Like / dislike buttons with counts, used on forum cards and the post page.
 * The active button reflects the current user's own reaction.
 */
const ReactionButtons: React.FC<ReactionButtonsProps> = ({
  likes,
  dislikes,
  userReaction,
  onReact,
  size = "middle",
}) => {
  const handleClick =
    (reaction: Reaction) => (event: React.MouseEvent<HTMLElement>) => {
      // Forum cards navigate to the post on click; a reaction should not.
      event.stopPropagation();
      onReact(reaction);
    };

  return (
    <Space className="reaction-buttons" size="small">
      <Button
        shape="round"
        size={size}
        className={`reaction-btn like${userReaction === "like" ? " active" : ""}`}
        icon={userReaction === "like" ? <LikeFilled /> : <LikeOutlined />}
        onClick={handleClick("like")}
        aria-label="Like"
        aria-pressed={userReaction === "like"}
      >
        {likes || 0}
      </Button>
      <Button
        shape="round"
        size={size}
        className={`reaction-btn dislike${userReaction === "dislike" ? " active" : ""}`}
        icon={
          userReaction === "dislike" ? <DislikeFilled /> : <DislikeOutlined />
        }
        onClick={handleClick("dislike")}
        aria-label="Dislike"
        aria-pressed={userReaction === "dislike"}
      >
        {dislikes || 0}
      </Button>
    </Space>
  );
};

export default ReactionButtons;
