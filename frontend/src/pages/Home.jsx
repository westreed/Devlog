import React from "react";
import PageTemplate from "components/common/pageTemplate";
import HeaderContainer from "containers/base/HeaderContainer";
import MainContainer from "containers/main/MainContainer";

function Home() {
  return (
    <PageTemplate>
      <HeaderContainer />
      <MainContainer />
    </PageTemplate>
  );
}

export default Home;
