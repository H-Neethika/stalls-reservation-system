import QRCode from "qrcode";

export const generateQRCode = async (data: string): Promise<string> => {
  try {
    const qrCodeDataURL = await QRCode.toDataURL(data, {
      width: 300,
      margin: 2,
      color: {
        dark: "#000000",
        light: "#FFFFFF",
      },
    });
    return qrCodeDataURL;
  } catch (error) {
    console.error("Error generating QR code:", error);
    throw error;
  }
};

export const generateQRCodeForReservation = async (
  reservationId: string,
  stallName: string,
  hallName: string
): Promise<string> => {
  const qrData = JSON.stringify({
    reservationId,
    stallName,
    hallName,
    timestamp: new Date().toISOString(),
  });
  return generateQRCode(qrData);
};
