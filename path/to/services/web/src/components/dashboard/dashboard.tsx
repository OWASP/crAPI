# complete code
import React from 'react';
import { Card } from 'antd';
import vehicleCardHeader from './vehicleCardHeader';
import vehicleCardContent from './vehicleCardContent';

interface Props {
  vehicle: any;
  handleVehicleServiceClick: () => void;
  handleContactMechanic: () => void;
}

const Dashboard: React.FC<Props> = ({
  vehicle,
  handleVehicleServiceClick,
  handleContactMechanic,
}) => {
  return (
    <Card>
      {vehicleCardHeader(vehicle, handleVehicleServiceClick, handleContactMechanic)}
      {vehicleCardContent(vehicle)}
    </Card>
  );
};

export default Dashboard;