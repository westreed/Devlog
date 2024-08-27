import { API } from "./Axios";

export const edit_comment_api = async (
  comment_id,
  content,
  files,
  isPrivate
) => {
  const requestBody = {
    content: content,
    files: files,
    isPrivate: isPrivate,
  };

  console.log("post edit_comment_api (", requestBody, ")");
  return await API.post(`/comments/${comment_id}`, requestBody)
    .then((response) => response)
    .catch((error) => {
      throw error;
    });
};

export const upload_comment_api = async (
  post,
  parent,
  content,
  files,
  isPrivate
) => {
  const requestBody = {
    post: post,
    parent: parent,
    content: content,
    files: files,
    isPrivate: isPrivate,
  };

  console.log("post upload_reply_comment_api (", requestBody, ")");
  return await API.post(`/comments`, requestBody)
    .then((response) => response)
    .catch((error) => {
      throw error;
    });
};