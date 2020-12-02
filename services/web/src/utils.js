import * as jwt from "jsonwebtoken";

export const getMapUrl = (lat, long) => `https://maps.google.com/maps?q=${lat},${long}&output=embed`;

export const isAccessTokenValid = (token) => {
  const decoded = jwt.decode(token);
  const currentTime = Math.floor(new Date().getTime() / 1000);
  if (decoded && currentTime < decoded.exp) return true;
  return false;
};

export const formatDateFromIso = (isoDate) => {
  const date = new Date(isoDate);
  return date.toDateString();
};