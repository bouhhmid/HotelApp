import { Component,OnInit} from '@angular/core';
import { HotelService } from 'src/app/services/hotel.service';

@Component({
  selector: 'app-client-home',
  templateUrl: './client-home.component.html',
  styleUrls: ['./client-home.component.css']
})
export class ClientHomeComponent  implements OnInit {
  hotels: any[] = [];

  constructor(private hotelservice : HotelService) {}
  // client-home.component.ts
ngOnInit() {
  this.hotelservice.getAllHotels().subscribe(hotels => {
    this.hotels = hotels;
  });
}


}
