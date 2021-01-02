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

import { Card, Steps } from "antd";

import React, { useState } from "react";

import PropTypes from "prop-types";

import NewEmailFormContainer from "../../containers/newEmailForm/newEmailForm";
import TokenFormContainer from "../../containers/tokenForm/tokenForm";

const { Step } = Steps;

const ChangeEmail = (props) => {
  const { history } = props;
  const steps = [
    {
      title: "New Email",
      component: NewEmailFormContainer,
    },
    {
      title: "Email Verification",
      component: TokenFormContainer,
    },
  ];

  const [email, setEmail] = useState("");
  const [currentStep, setCurrentStep] = useState(0);

  const handleStepChange = (step) => setCurrentStep(step);

  const handleEmailChange = (newEmail) => setEmail(newEmail);

  const CurrentComponent = steps[currentStep].component;

  return (
    <div className="container">
      <Card title="Change Email" bordered={false} className="form-card">
        <Steps current={currentStep} size="small">
          {steps.map((step) => (
            <Step key={step.title} title={step.title} />
          ))}
        </Steps>
        <div className="steps-content">
          <CurrentComponent
            currentStep={currentStep}
            setCurrentStep={handleStepChange}
            email={email}
            onMailChange={handleEmailChange}
            history={history}
          />
        </div>
      </Card>
    </div>
  );
};

ChangeEmail.propTypes = {
  history: PropTypes.object,
};

export default ChangeEmail;
