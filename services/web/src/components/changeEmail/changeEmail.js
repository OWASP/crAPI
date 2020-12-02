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
