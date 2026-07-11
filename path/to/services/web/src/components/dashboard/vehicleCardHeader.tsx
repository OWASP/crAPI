# complete code
import React from 'react';

interface Props {
  vehicle: any;
  handleVehicleServiceClick: () => void;
  handleContactMechanic: () => void;
}

const VehicleCardHeader: React.FC<Props> = ({
  vehicle,
  handleVehicleServiceClick,
  handleContactMechanic,
}) => {
  return (
    <div>
      <h2>{vehicle.vin}</h2>
      <button onClick={handleVehicleServiceClick}>Vehicle Service History</button>
      <button onClick={handleContactMechanic}>Contact Mechanic</button>
    </div>
  );
};

export default VehicleCardHeader;