db=db.getSiblingDB('notification_db');
const messageTemplates={
  ORDER_PAID:'Thanh toán đã được xác nhận cho đơn hàng của bạn.',
  ORDER_SHIPPED:'Đơn hàng đã bàn giao cho đơn vị vận chuyển.',
  COMMENT_REPLIED:'Có người vừa trả lời thảo luận bạn đang theo dõi.',
  RETURN_APPROVED:'Yêu cầu trả hàng đã được chấp thuận.',
  PROMOTION_REMINDER:'Ưu đãi bạn quan tâm sắp hết hạn.'
};
const unreadRatio=34;
function eventWeight(i){const x=i%100;return x<34?'ORDER_SHIPPED':x<59?'ORDER_PAID':x<76?'COMMENT_REPLIED':x<88?'RETURN_APPROVED':'PROMOTION_REMINDER';}
let batch=[];
for(let i=1;i<=1000000;i++){
  const type=eventWeight(i);
  const powerUser=i%100<15;
  const userNo=powerUser?1+(i%8000):8001+(i%242000);
  batch.push({insertOne:{document:{
    _id:'NTF-L-'+i,userId:'customer-'+String(userNo).padStart(7,'0'),type,
    title:type==='ORDER_SHIPPED'?'Đơn hàng đang được giao':type==='ORDER_PAID'?'Thanh toán thành công':type==='RETURN_APPROVED'?'Trả hàng được duyệt':'Cập nhật mới',
    message:messageTemplates[type],readAt:(i%100)<unreadRatio?null:new Date(Date.now()-(i%30)*3600000),
    createdAt:new Date(Date.now()-(i%120)*86400000-((i%24)*3600000)),payload:{sequence:i,eventWeight:type}
  }}});
  if(batch.length===1000){db.notification.bulkWrite(batch,{ordered:false});batch=[];}
}
if(batch.length)db.notification.bulkWrite(batch,{ordered:false});
