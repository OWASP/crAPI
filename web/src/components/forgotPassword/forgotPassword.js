import "./forgotPassword.css";
import { Card, Steps } from "antd";

import React, { useState } from "react";

import PropTypes from "prop-types";

import EmailFormContainer from "../../containers/emailForm/emailForm";
import OTPFormContainer from "../../containers/otpForm/otpForm";

const { Step } = Steps;

const ForgotPassword = (props) => {
  const { history } = props;

  const steps = [
    {
      title: "Email Verification",
      component: EmailFormContainer,
    },
    {
      title: "Reset Password",
      component: OTPFormContainer,
    },
  ];

  const [email, setEmail] = useState("");
  const [currentStep, setCurrentStep] = useState(0);

  const handleStepChange = (step) => setCurrentStep(step);

  const handleEmailChange = (newEmail) => setEmail(newEmail);

  const CurrentComponent = steps[currentStep].component;
  return (
    <div className="container">
      <Card title="Forgot Password" bordered={false} className="form-card">
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
ForgotPassword.propTypes = {
  history: PropTypes.object,
};

export default ForgotPassword;
