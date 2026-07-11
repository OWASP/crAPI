# complete code
import React from 'react';

interface Props {
  vehicle: any;
}

const VehicleCardContent: React.FC<Props> = ({ vehicle }) => {
  return (
    <div>
      <h3>{vehicle.make} {vehicle.model}</h3>
      <p>{vehicle.year}</p>
    </div>
  );
};

export default VehicleCardContent;