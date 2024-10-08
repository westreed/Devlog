import React from "react";
import Profile from "components/side/profile";
import Category from "components/category/display";

function LeftSideContainer() {
  return (
    <aside>
      <Profile />
      <Category />
    </aside>
  );
}

export default LeftSideContainer;
