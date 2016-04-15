/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Zhouzhiqiang <zhiqiang.zhou@ingenic.com>
 *
 *  Elf/IDWS Project
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; either version 2 of the License, or (at your
 *  option) any later version.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package com.ingenic.iwds.smartspeech;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ingenic.iwds.smartspeech.business.RemoteAction;
import com.ingenic.iwds.smartspeech.business.RemoteAlternativesource;
import com.ingenic.iwds.smartspeech.business.RemoteAppObject;
import com.ingenic.iwds.smartspeech.business.RemoteBusiness;
import com.ingenic.iwds.smartspeech.business.RemoteBusinessObject;
import com.ingenic.iwds.smartspeech.business.RemoteContactsObject;
import com.ingenic.iwds.smartspeech.business.RemoteDatasource;
import com.ingenic.iwds.smartspeech.business.RemoteDateTime;
import com.ingenic.iwds.smartspeech.business.RemoteDialogObject;
import com.ingenic.iwds.smartspeech.business.RemoteFlightObject;
import com.ingenic.iwds.smartspeech.business.RemoteHotelObject;
import com.ingenic.iwds.smartspeech.business.RemoteMapObject;
import com.ingenic.iwds.smartspeech.business.RemoteMessageObject;
import com.ingenic.iwds.smartspeech.business.RemoteMusicObject;
import com.ingenic.iwds.smartspeech.business.RemoteNews;
import com.ingenic.iwds.smartspeech.business.RemoteNewsItem;
import com.ingenic.iwds.smartspeech.business.RemoteNewsObject;
import com.ingenic.iwds.smartspeech.business.RemoteNewsParams;
import com.ingenic.iwds.smartspeech.business.RemoteOtherObject;
import com.ingenic.iwds.smartspeech.business.RemoteRestaurantObject;
import com.ingenic.iwds.smartspeech.business.RemoteResult;
import com.ingenic.iwds.smartspeech.business.RemoteScheduleObject;
import com.ingenic.iwds.smartspeech.business.RemoteShopDatasource;
import com.ingenic.iwds.smartspeech.business.RemoteStockDate;
import com.ingenic.iwds.smartspeech.business.RemoteStockObject;
import com.ingenic.iwds.smartspeech.business.RemoteTelephoneObject;
import com.ingenic.iwds.smartspeech.business.RemoteTrainObject;
import com.ingenic.iwds.smartspeech.business.RemoteTranslationObject;
import com.ingenic.iwds.smartspeech.business.RemoteWeatherCondition;
import com.ingenic.iwds.smartspeech.business.RemoteWeatherForecast;
import com.ingenic.iwds.smartspeech.business.RemoteWeatherObject;
import com.ingenic.iwds.smartspeech.business.RemoteWebsiteObject;

public class ProxyUtils {

    /**
     * 对外开放的获取json中第一个word的方法
     * 
     * @param json
     * @return
     */
    static String parseJsonResult(String json) {
        return parseIatResult(json);
    }

    /**
     * 对外开放的获取RemoteBusiness实例方法
     * 
     * @param xml
     *            语义理解返回的xml的字符串内容
     * @return 初始化好的RemoteBusiness
     */
    static RemoteBusiness parseXmlResult(String xml) {
        return getBusinessModel(xml);
    }

    /**
     * 私有的获取json中第一个word的方法
     * 
     * @param json
     *            语义理解返回的json的字符串内容
     * @return
     */
    private static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                for (int j = 0; j < items.length(); j++) {
                    JSONObject obj = items.getJSONObject(j);
                    ret.append(obj.getString("w"));
                }
            }
        } catch (Exception e) {
            return null;
        }
        return ret.toString();
    }

    /**
     * 私有的获取RemoteBusiness实例的方法
     * 
     * @param xml
     *            语义理解后返回的xml字符串
     * @return 初始化好的RemoteBusiness
     */
    private static RemoteBusiness getBusinessModel(String xml) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        RemoteBusiness business = new RemoteBusiness();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            Document document = builder.parse(is);
            Element root = document.getDocumentElement();
            business.mState = getNodeContent(RemoteBusiness.RAWSTATUS, root);
            business.mRawText = getNodeContent(RemoteBusiness.RAWRAWTEXT, root);
            business.mErrorCode = getNodeContent(RemoteBusiness.RAWERRORCODE,
                    root);
            business.mDesc = getNodeContent(RemoteBusiness.RAWDESC, root);
            Node time_stamp = root.getElementsByTagName(
                    RemoteBusiness.RAWTIMESTAMP).item(0);
            business.mTimeStamp = getNoteDatetime(time_stamp);
            Element result = (Element) root.getElementsByTagName(
                    RemoteBusiness.RAWRESULT).item(0);
            business.mResult = getResult(result);
        } catch (ParserConfigurationException e) {
            return null;
        } catch (SAXException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return business;
    }

    /**
     * 获得RemoteResult实例的方法
     * 
     * @param result
     *            result节点对象
     * @return RemoteResult对象
     */
    private static RemoteResult getResult(Element result) {
        RemoteResult resultmodel = new RemoteResult();
        Element action = null;
        Element object = null;
        NodeList objects = null;
        if (result != null) {
            Node focusNode = result.getElementsByTagName(RemoteResult.RAWFOCUS)
                    .item(0);
            resultmodel.mFocus = focusNode.getTextContent().trim();
            action = (Element) result.getElementsByTagName(
                    RemoteResult.RAWACTION).item(0);
            if (action != null) {
                Node opration = action.getElementsByTagName(
                        RemoteAction.RAWOPERATION).item(0);
                String opratrionstr = (opration == null) ? null : opration
                        .getTextContent().trim();
                Node chanal = action.getElementsByTagName(
                        RemoteAction.RAWCHANNAL).item(0);
                String chanalstr = (chanal == null) ? null : chanal
                        .getTextContent().trim();
                resultmodel.mAction = new RemoteAction(opratrionstr, chanalstr);
            } else {
                resultmodel.mAction = null;
            }
            Node contentNode = result.getElementsByTagName(
                    RemoteResult.RAWCONTENT).item(0);
            if (contentNode != null)
                resultmodel.mContent = contentNode.getTextContent().trim();
            Node contenttypeNode = result.getElementsByTagName(
                    RemoteResult.RAWCONTENTTYPE).item(0);
            if (contenttypeNode != null)
                resultmodel.mContentType = contenttypeNode.getTextContent()
                        .trim();
            object = (Element) result.getElementsByTagName(
                    RemoteResult.RAWOBJECT).item(0);
            objects = result.getElementsByTagName(RemoteResult.RAWOBJECT);
            if (resultmodel.mFocus.equals(RemoteTelephoneObject.sFocus)) {
                resultmodel.mObject = getTelephoneObject(object);
            } else if (resultmodel.mFocus.equals(RemoteMessageObject.sFocus)) {
                resultmodel.mObject = null;
                resultmodel.mObjects = getMessageObject(objects);
            } else if (resultmodel.mFocus.equals(RemoteAppObject.sFocus)) {
                resultmodel.mObject = getAppObject(object);
            } else if (resultmodel.mFocus.equals(RemoteContactsObject.sFocus)) {
                resultmodel.mObject = null;
                resultmodel.mObjects = getContactsObject(objects);
            } else if (resultmodel.mFocus.equals(RemoteScheduleObject.sFocus)) {
                resultmodel.mObject = getScheduleObject(object);
            } else if (resultmodel.mFocus.equals(RemoteWebsiteObject.sFocus)) {
                resultmodel.mObject = getWebsiteObject(object);
            } else if (resultmodel.mFocus.equals(RemoteMapObject.sFocus)) {
                resultmodel.mObject = getMapObject(object);
            } else if (resultmodel.mFocus.equals(RemoteStockObject.sFocus)) {
                resultmodel.mObject = getStockObject(object);
            } else if (resultmodel.mFocus.equals(RemoteWeatherObject.sFocus)) {
                resultmodel.mObject = getWeatherObject(object);
            } else if (resultmodel.mFocus.equals(RemoteDialogObject.sFocus)) {
                resultmodel.mObject = getDialogObject(object);
            } else if (resultmodel.mFocus.equals(RemoteOtherObject.sFocus)) {
                resultmodel.mObject = getOthersearchObject(object);
            } else if (resultmodel.mFocus.equals(RemoteRestaurantObject.sFocus)) {
                resultmodel.mObject = getRestaurantObject(object);
            } else if (resultmodel.mFocus.equals(RemoteMusicObject.sFocus)) {
                resultmodel.mObject = getMusicObject(object);
            } else if (resultmodel.mFocus
                    .equals(RemoteTranslationObject.sFocus)) {
                resultmodel.mObject = null;
                resultmodel.mObjects = getTranslationObject(objects);
            } else if (resultmodel.mFocus.equals(RemoteFlightObject.sFocus)) {
                resultmodel.mObject = getFlightObject(object);
            } else if (resultmodel.mFocus.equals(RemoteTrainObject.sFocus)) {
                resultmodel.mObject = getTrainObject(object);
            } else if (resultmodel.mFocus.equals(RemoteNewsObject.sFocus)) {
                resultmodel.mObject = getNewsObject(object);
            } else if (resultmodel.mFocus.equals(RemoteHotelObject.sFocus)) {
                resultmodel.mObject = getHotelObject(object);
            }
        }
        return resultmodel;
    }

    /**
     * 获得HotelObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return HotelObject对象
     */
    private static RemoteBusinessObject getHotelObject(Element object) {
        RemoteHotelObject hotelobject = null;
        if (object != null) {
            hotelobject = new RemoteHotelObject();
            hotelobject.ObjectName = RemoteHotelObject.sFocus;
            Element date_sourceNode = (Element) object.getElementsByTagName(
                    RemoteHotelObject.RAWDATASOURCE).item(0);
            hotelobject.mDataSource = getNodeDateSource(date_sourceNode);
            Node urlNode = object
                    .getElementsByTagName(RemoteHotelObject.RAWURL).item(0);
            if (urlNode != null)
                hotelobject.mUrl = urlNode.getTextContent().trim();
        }
        return hotelobject;
    }

    /**
     * 获得RemoteBusinessObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return NewsObject对象
     */
    private static RemoteBusinessObject getNewsObject(Element object) {
        RemoteNewsObject newsobject = null;
        if (object != null) {
            newsobject = new RemoteNewsObject();
            newsobject.ObjectName = RemoteNewsObject.sFocus;
            Element data_sourceNode = (Element) object.getElementsByTagName(
                    RemoteNewsObject.RawDataSource).item(0);
            newsobject.mDataSource = getNodeDateSource(data_sourceNode);
            newsobject.mServerUrl = getNodeContent(
                    RemoteNewsObject.RawServerUrl, object);
            Element newsparamsNode = (Element) object.getElementsByTagName(
                    RemoteNewsObject.RawParam).item(0);
            if (newsparamsNode != null) {
                newsobject.mParam = new RemoteNewsParams();
                newsobject.mParam.mMedia = getNodeContent(
                        RemoteNewsParams.RAWMEDIA, newsparamsNode);
                newsobject.mParam.mCategory = getNodeContent(
                        RemoteNewsParams.RAWCATEGORY, newsparamsNode);
                newsobject.mParam.mLoc = getNodeContent(
                        RemoteNewsParams.RAWLOC, newsparamsNode);
                newsobject.mParam.mKeyword = getNodeContent(
                        RemoteNewsParams.RAWKEYWORD, newsparamsNode);
                Node datetimeNode = newsparamsNode.getElementsByTagName(
                        RemoteNewsParams.RAWDATETIME).item(0);
                if (datetimeNode != null) {
                    Node dateNode = datetimeNode.getAttributes().getNamedItem(
                            RemoteNewsParams.ATTRDATE);
                    if (dateNode != null)
                        newsobject.mParam.mDateTime = dateNode.getTextContent()
                                .trim();
                }
                newsobject.mParam.mNewsid = getNodeContent(
                        RemoteNewsParams.RAWNEWSID, newsparamsNode);
                newsobject.mParam.mRemains = getNodeContent(
                        RemoteNewsParams.RAWREMAINS, newsparamsNode);
                newsobject.mParam.mcategoryids = getNodeContent(
                        RemoteNewsParams.RAWCATEGORYIDS, newsparamsNode);
            }
            Element newsNode = (Element) object.getElementsByTagName(
                    RemoteNewsObject.RawNews).item(0);
            newsobject.mNews = getNews(newsNode);
        }
        return newsobject;
    }

    /**
     * 获得TrainObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return TrainObject对象
     */
    private static RemoteBusinessObject getTrainObject(Element object) {
        RemoteTrainObject trainobject = null;
        if (object != null) {
            trainobject = new RemoteTrainObject();
            trainobject.ObjectName = RemoteTrainObject.sFocus;
            Node startdatetimeNode = object.getElementsByTagName(
                    RemoteTrainObject.RAWDATETIME).item(0);
            trainobject.mStartDateTime = getNoteDatetime(startdatetimeNode);
            Node enddatetimeNode = object.getElementsByTagName(
                    RemoteTrainObject.RAWDATETIME).item(1);
            trainobject.mEndDateTime = getNoteDatetime(enddatetimeNode);
            trainobject.mPoints = new ArrayList<String>();
            Node firstpointNode = object.getElementsByTagName(
                    RemoteTrainObject.RAWPOINT).item(0);
            if (firstpointNode != null)
                trainobject.mPoints.add(firstpointNode.getTextContent().trim());
            Node secondpointNode = object.getElementsByTagName(
                    RemoteTrainObject.RAWPOINT).item(1);
            if (secondpointNode != null)
                trainobject.mPoints
                        .add(secondpointNode.getTextContent().trim());
            trainobject.mUrl = getNodeContent(RemoteTrainObject.RAWURL, object);
        }
        return trainobject;
    }

    /**
     * 获取FlightObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return FlightObject对象
     */
    private static RemoteBusinessObject getFlightObject(Element object) {
        RemoteFlightObject flightobject = null;
        if (object != null) {
            flightobject = new RemoteFlightObject();
            flightobject.ObjectName = RemoteFlightObject.sFocus;
            Node startdatetimeNode = object.getElementsByTagName(
                    RemoteFlightObject.RAWDATETIME).item(0);
            flightobject.mStartDateTime = getNoteDatetime(startdatetimeNode);
            Node enddatetimeNode = object.getElementsByTagName(
                    RemoteFlightObject.RAWDATETIME).item(1);
            flightobject.mEndDateTime = getNoteDatetime(enddatetimeNode);
            flightobject.mPoints = new ArrayList<String>();
            Node firstpointNode = object.getElementsByTagName(
                    RemoteFlightObject.RAWPOINT).item(0);
            if (firstpointNode != null)
                flightobject.mPoints
                        .add(firstpointNode.getTextContent().trim());
            Node secondpointNode = object.getElementsByTagName(
                    RemoteFlightObject.RAWPOINT).item(1);
            if (secondpointNode != null)
                flightobject.mPoints.add(secondpointNode.getTextContent()
                        .trim());
            flightobject.mUrl = getNodeContent(RemoteFlightObject.RAWURL,
                    object);
        }
        return flightobject;
    }

    /**
     * 获得TranslationObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return TranslationObject对象
     */
    private static List<RemoteBusinessObject> getTranslationObject(
            NodeList objects) {
        int lenth = objects.getLength();
        List<RemoteBusinessObject> translationobjects = null;
        if (lenth > 0) {
            translationobjects = new ArrayList<RemoteBusinessObject>();
            for (int i = 0; i < lenth; i++) {
                Element object = (Element) objects.item(i);
                RemoteTranslationObject translationobject = null;
                if (object != null) {
                    translationobject = new RemoteTranslationObject();
                    translationobject.ObjectName = RemoteTranslationObject.sFocus;
                    translationobject.mEngineName = getNodeContent(
                            RemoteTranslationObject.RAWENGINENAME, object);
                    translationobject.mEngineType = getNodeContent(
                            RemoteTranslationObject.RAWENGINETYPE, object);
                    Node originalNode = object.getElementsByTagName(
                            RemoteTranslationObject.RAWORIGINAL).item(0);
                    Node translatedNode = object.getElementsByTagName(
                            RemoteTranslationObject.RAWTRANSLATED).item(0);
                    if (originalNode != null) {
                        translationobject.mOriginal = originalNode
                                .getTextContent().trim();

                        NamedNodeMap originalLangNNM = originalNode
                                .getAttributes();
                        {
                            Node original_lang = originalLangNNM
                                    .getNamedItem(RemoteTranslationObject.RAWORIGINALLANG);

                            if (original_lang != null)
                                translationobject.mOriginalLang = original_lang
                                        .getTextContent().trim();
                            translationobjects.add(translationobject);
                        }
                    }
                    if (translatedNode != null) {
                        translationobject.mTranslated = translatedNode
                                .getTextContent().trim();
                        NamedNodeMap translatedLangNNM = translatedNode
                                .getAttributes();
                        if (translatedLangNNM != null) {
                            Node translated_lang = translatedLangNNM
                                    .getNamedItem(RemoteTranslationObject.RAWORIGINALLANG);

                            if (translated_lang != null)
                                translationobject.mTranslatedLang = translated_lang
                                        .getTextContent().trim();
                        }
                    }
                }
            }
        }
        return translationobjects;
    }

    /**
     * 获得MusicObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return MusicObject对象
     */
    private static RemoteBusinessObject getMusicObject(Element object) {

        RemoteMusicObject musicobject = null;
        if (object != null) {
            musicobject = new RemoteMusicObject();
            musicobject.ObjectName = RemoteMusicObject.sFocus;
            musicobject.mSinger = getNodeContent(RemoteMusicObject.RAWSINGER,
                    object);
            musicobject.mSong = getNodeContent(RemoteMusicObject.RAWSONG,
                    object);
            musicobject.mServerUrl = getNodeContent(
                    RemoteMusicObject.RAWSERVERURL, object);
            musicobject.mCategory = getNodeContent(
                    RemoteMusicObject.RAWCATEGORY, object);
            musicobject.mMsResponse = getNodeContent(
                    RemoteMusicObject.RAWMSRESPONSE, object);
        }
        return musicobject;
    }

    /**
     * 获得RestaurantObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return RestaurantObject对象
     */
    private static RemoteBusinessObject getRestaurantObject(Element object) {
        RemoteRestaurantObject restaurantobject = null;
        if (object != null) {
            restaurantobject = new RemoteRestaurantObject();
            restaurantobject.ObjectName = RemoteWebsiteObject.sFocus;
            restaurantobject.mDataType = getNodeContent(
                    RemoteRestaurantObject.RAWDATATYPE, object);
            restaurantobject.mLocJudgement = getNodeContent(
                    RemoteRestaurantObject.RAWLOCJUDGEMENT, object);
            restaurantobject.mUrl = getNodeContent(
                    RemoteRestaurantObject.RAWURL, object);
            restaurantobject.mKeyword = getNodeContent(
                    RemoteRestaurantObject.RAWKEYWORD, object);
            restaurantobject.mServerUrl = getNodeContent(
                    RemoteRestaurantObject.RAWSERVERURL, object);
            restaurantobject.mPageIndex = getNodeContent(
                    RemoteRestaurantObject.RAWPAGEINDEX, object);
            restaurantobject.mPageTotal = getNodeContent(
                    RemoteRestaurantObject.RAWPAGETOTAL, object);
            restaurantobject.mRecorCount = getNodeContent(
                    RemoteRestaurantObject.RAWRECORDCOUNT, object);

            restaurantobject.mDataSource = getNodeotherDatesource(
                    RemoteRestaurantObject.RAWDATA_SOURCE, object);
            restaurantobject.mCity = getNodeotherDatesource(
                    RemoteRestaurantObject.RAWCITY, object);
            restaurantobject.mCategory = getNodeotherDatesource(
                    RemoteRestaurantObject.RAWCATEGORY, object);
            Element alternative_sourceNodeElement = (Element) object
                    .getElementsByTagName(
                            RemoteRestaurantObject.RAWALTERNATIVESOURCE)
                    .item(0);
            if (alternative_sourceNodeElement != null) {
                restaurantobject.mAlternativeSource = new RemoteAlternativesource(
                        getNodeContent(RemoteAlternativesource.RAWURL,
                                alternative_sourceNodeElement), getNodeContent(
                                RemoteAlternativesource.RAWNAME,
                                alternative_sourceNodeElement));
            }
            Element shoplistNodes = (Element) object.getElementsByTagName(
                    RemoteRestaurantObject.RAWSHOPLIST).item(0);
            if (shoplistNodes != null) {
                NodeList shoplistNode = shoplistNodes
                        .getElementsByTagName(RemoteRestaurantObject.RAWSHOP);
                if (shoplistNode != null) {
                    List<RemoteShopDatasource> shoplist = new ArrayList<RemoteShopDatasource>();
                    int shoplenth = shoplistNode.getLength();
                    for (int i = 0; i < shoplenth; i++) {
                        Element shopNode = (Element) shoplistNode.item(i);
                        RemoteShopDatasource shop = getShop(shopNode);
                        shoplist.add(shop);
                    }
                    restaurantobject.mShopList = shoplist;
                }
            }
        }
        return restaurantobject;
    }

    /**
     * 获得OthersearchObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return OthersearchObject对象
     */
    private static RemoteBusinessObject getOthersearchObject(Element object) {
        RemoteOtherObject otherobject = null;
        if (object != null) {
            otherobject = new RemoteOtherObject();
            otherobject.ObjectName = RemoteTelephoneObject.sFocus;
            otherobject.mName = getNodeContent(RemoteOtherObject.RAWNAME,
                    object);
            otherobject.mUrl = getNodeContent(RemoteOtherObject.RAWURL, object);
        }
        return otherobject;
    }

    /**
     * 获得DialogObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return DialogObject对象
     */
    private static RemoteBusinessObject getDialogObject(Element object) {
        RemoteDialogObject dialogobject = null;
        if (object != null) {
            dialogobject = new RemoteDialogObject();
            dialogobject.ObjectName = RemoteDialogObject.sFocus;
            dialogobject.mTopic = getNodeContent(RemoteDialogObject.RAWTOPIC,
                    object);
            dialogobject.mQuestion = getNodeContent(
                    RemoteDialogObject.RAWQUESTION, object);
            dialogobject.mAnswer = getNodeContent(RemoteDialogObject.RAWANSWER,
                    object);
            dialogobject.mPicUrl = getNodeContent(RemoteDialogObject.RAWPICURL,
                    object);
            dialogobject.mPageUrl = getNodeContent(
                    RemoteDialogObject.RAWPAGEURL, object);
            dialogobject.mAudioUrl = getNodeContent(
                    RemoteDialogObject.RAWAUDIOURL, object);
        }
        return dialogobject;
    }

    /**
     * 获得WeatherObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return WeatherObject对象
     */
    private static RemoteBusinessObject getWeatherObject(Element object) {
        RemoteWeatherObject weatherobject = null;
        if (object != null) {
            weatherobject = new RemoteWeatherObject();
            weatherobject.ObjectName = RemoteWeatherObject.sFocus;
            Element last_updateNode = (Element) object.getElementsByTagName(
                    RemoteWeatherObject.RAWLASTUPDATE).item(0);
            Element interest_datetimeNode = (Element) object
                    .getElementsByTagName(RemoteWeatherObject.RAWDATETIME)
                    .item(0);
            NodeList forecastNodes = object
                    .getElementsByTagName(RemoteWeatherObject.RAWFORECAST);
            weatherobject.mCity = getNodeContent(RemoteWeatherObject.RAWCITY,
                    object);
            Element datsource = (Element) object.getElementsByTagName(
                    RemoteWeatherObject.RAWDATESOURCE).item(0);
            weatherobject.mDataSource = getNodeDateSource(datsource);
            Node datetimeNode = null;
            if (last_updateNode != null)
                datetimeNode = last_updateNode.getElementsByTagName(
                        RemoteWeatherObject.RAWDATETIME).item(0);
            weatherobject.mLastUpdate = getNoteDatetime(datetimeNode);
            datetimeNode = null;
            if (interest_datetimeNode != null)
                datetimeNode = interest_datetimeNode.getElementsByTagName(
                        RemoteWeatherObject.RAWDATETIME).item(0);
            weatherobject.mInterestDatetime = getNoteDatetime(datetimeNode);
            if (forecastNodes != null) {
                int forecastNodelenth = forecastNodes.getLength();
                weatherobject.mForecasts = new ArrayList<RemoteWeatherForecast>();
                for (int i = 0; i < forecastNodelenth; i++) {
                    Element forecastNode = (Element) forecastNodes.item(i);
                    weatherobject.mForecasts.add(getForecast(forecastNode));
                }
            }
        }
        return weatherobject;
    }

    /**
     * 获得StockObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return StockObject对象
     */
    private static RemoteBusinessObject getStockObject(Element object) {
        RemoteStockObject stockobject = null;
        stockobject = new RemoteStockObject();
        if (object != null) {
            stockobject = new RemoteStockObject();
            stockobject.ObjectName = RemoteStockObject.sFocus;
            stockobject.mName = getNodeContent(RemoteStockObject.RAWNAME,
                    object);
            stockobject.mCode = getNodeContent(RemoteStockObject.RAWCODE,
                    object);
            stockobject.mCategory = getNodeContent(
                    RemoteStockObject.RAWCATEGORY, object);
            stockobject.mType = getNodeContent(RemoteStockObject.RAWTYPE,
                    object);
            stockobject.mUrl = getNodeContent(RemoteStockObject.RAWURL, object);
            Element dataNode = (Element) object.getElementsByTagName(
                    RemoteStockObject.RAWDATA).item(0);
            stockobject.mData = getStockData(dataNode);
        }
        return stockobject;
    }

    /**
     * 获得MapObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return MapObject对象
     */
    private static RemoteBusinessObject getMapObject(Element object) {
        RemoteMapObject mapobject = null;
        if (object != null) {
            mapobject = new RemoteMapObject();
            mapobject.ObjectName = RemoteMapObject.sFocus;
            NodeList pointlist = object
                    .getElementsByTagName(RemoteMapObject.RAWPOINT);
            int lenth = pointlist.getLength();
            if (lenth == 1) {
                Element firstpointNode = (Element) pointlist.item(0);
                if (firstpointNode != null) {
                    mapobject.mFirstPoint = firstpointNode.getTextContent()
                            .trim();
                    Node client = firstpointNode.getAttributes().getNamedItem(
                            RemoteMapObject.ATTRCLIENT);
                    if (client != null)
                        mapobject.mFirstPointClient = client.getTextContent()
                                .trim();
                }
            }
            if (lenth == 2) {
                Element firstpointNode = (Element) pointlist.item(0);
                Element secondpointNode = (Element) pointlist.item(1);
                mapobject.mFirstPoint = firstpointNode.getTextContent().trim();
                Node firestattr = firstpointNode.getAttributes().getNamedItem(
                        RemoteMapObject.ATTRCLIENT);
                if (firestattr != null)
                    mapobject.mFirstPointClient = firestattr.getTextContent();
                mapobject.mSecondPoint = secondpointNode.getTextContent()
                        .trim();
                Node secondattr = secondpointNode.getAttributes().getNamedItem(
                        RemoteMapObject.ATTRCLIENT);
                if (secondattr != null)
                    mapobject.mSecondPointClient = secondattr.getTextContent();
            }
            mapobject.mUrl = getNodeContent(RemoteMapObject.RAWURL, object);
        }
        return mapobject;
    }

    /**
     * 获得WebsiteObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return WebsiteObject对象
     */
    private static RemoteBusinessObject getWebsiteObject(Element object) {

        RemoteWebsiteObject websiteobject = null;
        if (object != null) {
            websiteobject = new RemoteWebsiteObject();
            websiteobject.ObjectName = RemoteWebsiteObject.sFocus;
            websiteobject.mName = getNodeContent(RemoteWebsiteObject.RAWNAME,
                    object);
            websiteobject.mCode = getNodeContent(RemoteWebsiteObject.RAWCODE,
                    object);
            websiteobject.mType = getNodeContent(RemoteWebsiteObject.RAWTYPE,
                    object);
        }
        return websiteobject;
    }

    /**
     * 获得ScheduleObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return ScheduleObject对象
     */
    private static RemoteBusinessObject getScheduleObject(Element object) {

        RemoteScheduleObject scheduleobject = null;
        if (object != null) {
            scheduleobject = new RemoteScheduleObject();
            scheduleobject.ObjectName = RemoteScheduleObject.sFocus;
            scheduleobject.mName = getNodeContent(RemoteScheduleObject.RAWNAME,
                    object);
            scheduleobject.mRepeat = getNodeContent(
                    RemoteScheduleObject.RAWREPEAT, object);
            Node datetimeNode = object.getElementsByTagName(
                    RemoteScheduleObject.RAWDATETIME).item(0);
            scheduleobject.mDateTime = getNoteDatetime(datetimeNode);
        }
        return scheduleobject;
    }

    /**
     * 获得ContactsObject实例的方法
     * 
     * @param objects
     *            object节点对象
     * @return ContactsObject对象
     */
    private static List<RemoteBusinessObject> getContactsObject(NodeList objects) {

        int lenth = objects.getLength();
        List<RemoteBusinessObject> contactsobjects = null;
        if (lenth > 0) {
            contactsobjects = new ArrayList<RemoteBusinessObject>();
            for (int i = 0; i < lenth; i++) {
                Element object = (Element) objects.item(i);
                RemoteContactsObject contactsobject = new RemoteContactsObject();
                contactsobject.ObjectName = RemoteContactsObject.sFocus;
                NodeList nameNodes = object
                        .getElementsByTagName(RemoteContactsObject.RAWNAME);
                int nameNodelenth = nameNodes.getLength();
                contactsobject.mNames = new ArrayList<String>();
                for (int j = 0; j < nameNodelenth; j++) {
                    Element nameNode = (Element) nameNodes.item(j);
                    if (nameNode != null)
                        contactsobject.mNames.add(nameNode.getTextContent()
                                .trim());
                }
                Node categoryNode = object.getElementsByTagName(
                        RemoteContactsObject.RAWCATEGORY).item(0);
                if (categoryNode != null)
                    contactsobject.mCategory = categoryNode.getTextContent()
                            .trim();
                Node codeNode = object.getElementsByTagName(
                        RemoteContactsObject.RAWCODE).item(0);
                if (codeNode != null)
                    contactsobject.mCode = codeNode.getTextContent().trim();
                contactsobjects.add(contactsobject);
            }
        }
        return contactsobjects;
    }

    /**
     * 获得AppObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return AppObject对象
     */
    private static RemoteBusinessObject getAppObject(Element object) {
        RemoteAppObject appobject = null;
        if (object != null) {
            appobject = new RemoteAppObject();
            appobject.ObjectName = RemoteAppObject.sFocus;
            Node nameNode = object
                    .getElementsByTagName(RemoteAppObject.RAWNAME).item(0);
            if (nameNode != null)
                appobject.mName = nameNode.getTextContent().trim();
            Node postdataNode = object.getElementsByTagName(
                    RemoteAppObject.RAWPOSTDATA).item(0);
            if (postdataNode != null)
                appobject.mPostdata = postdataNode.getTextContent().trim();
            Node searchurlNode = object.getElementsByTagName(
                    RemoteAppObject.RAWSEARCHURL).item(0);
            if (searchurlNode != null)
                appobject.mSearchurl = searchurlNode.getTextContent().trim();
        }
        return appobject;
    }

    /**
     * 获得MessageObject实例组的方法
     * 
     * @param objects
     *            object节点对象
     * @return Messageobject的List表
     */
    private static List<RemoteBusinessObject> getMessageObject(NodeList objects) {
        int lenth = objects.getLength();
        List<RemoteBusinessObject> messageobjects = null;
        if (lenth > 0) {
            messageobjects = new ArrayList<RemoteBusinessObject>();
            for (int i = 0; i < lenth; i++) {
                Element object = (Element) objects.item(i);
                RemoteMessageObject messageobject = new RemoteMessageObject();
                messageobject.ObjectName = RemoteMessageObject.sFocus;

                NodeList names = object
                        .getElementsByTagName(RemoteMessageObject.RAWNAME);
                int namelenth = object.getElementsByTagName(
                        RemoteMessageObject.RAWNAME).getLength();
                messageobject.mNames = new ArrayList<String>();
                for (int j = 0; j < namelenth; j++) {
                    messageobject.mNames.add(names.item(j).getTextContent()
                            .trim());
                }
                Node categoryNode = object.getElementsByTagName(
                        messageobject.RAWCATEGORY).item(0);
                if (categoryNode != null)
                    messageobject.mCategory = categoryNode.getTextContent()
                            .trim();
                Node nametypeNode = object.getElementsByTagName(
                        messageobject.RAWNAMETYPE).item(0);
                if (nametypeNode != null)
                    messageobject.mNameType = nametypeNode.getTextContent()
                            .trim();
                messageobjects.add(messageobject);
            }
        }
        return messageobjects;
    }

    /**
     * 获得TelephoneObject实例的方法
     * 
     * @param object
     *            object节点对象
     * @return TelephoneObject对象
     */
    private static RemoteBusinessObject getTelephoneObject(Element object) {
        RemoteTelephoneObject telephoneobject = null;
        if (object != null) {
            telephoneobject = new RemoteTelephoneObject();
            telephoneobject.ObjectName = RemoteTelephoneObject.sFocus;
            Node nameNode = object.getElementsByTagName(
                    RemoteTelephoneObject.RAWNAME).item(0);
            if (nameNode != null)
                telephoneobject.mName = nameNode.getTextContent().trim();
            Node categoryNode = object.getElementsByTagName(
                    RemoteTelephoneObject.RAWCATEGORY).item(0);
            if (categoryNode != null)
                telephoneobject.mCategory = categoryNode.getTextContent()
                        .trim();
        }
        return telephoneobject;
    }

    /**
     * 获得 RemoteDateTime实例的方法
     * 
     * @param datetimeNode
     *            datetime节点对象
     * @return RemoteDateTime对象
     */
    private static RemoteDateTime getNoteDatetime(Node datetimeNode) {
        RemoteDateTime datetime = null;
        if (datetimeNode != null) {
            NamedNodeMap nodmap = datetimeNode.getAttributes();
            Node attrtime = nodmap.getNamedItem(RemoteDateTime.ROWTIME);
            Node attrdate = nodmap.getNamedItem(RemoteDateTime.ROWDATE);
            datetime = new RemoteDateTime(attrdate == null ? null : attrdate
                    .getTextContent().trim(), attrtime == null ? null
                    : attrtime.getTextContent().trim());
        }
        return datetime;
    }

    /**
     * 获得其他Datasource数据类型节点的 RemoteDatasource 实例的方法
     * 
     * @param tag
     *            其他Datasource数据类型节点的节点名
     * @param datasourcefatherNode
     *            其他Datasource数据类型节点的父节点对象
     * @return RemoteDatasource对象
     */
    private static RemoteDatasource getNodeotherDatesource(String tag,
            Element datasourcefatherNode) {

        RemoteDatasource datesource = null;
        Element datasourceNode = (Element) datasourcefatherNode
                .getElementsByTagName(tag).item(0);
        if (datasourceNode != null) {
            datesource = new RemoteDatasource(getNodeContent(
                    RemoteDatasource.ATTRID, datasourceNode), getNodeContent(
                    RemoteDatasource.ATTRNAME, datasourceNode));
        }
        return datesource;
    }

    /**
     * 获得 RemoteDatasource实例的方法
     * 
     * @param datasourceNode
     *            datasource节点对象
     * @return RemoteDatasource对象
     */
    private static RemoteDatasource getNodeDateSource(Element datasourceNode) {

        RemoteDatasource datesource = null;
        if (datasourceNode != null) {
            datesource = new RemoteDatasource(getNodeContent(
                    RemoteDatasource.ATTRID, datasourceNode), getNodeContent(
                    RemoteDatasource.ATTRNAME, datasourceNode));
        }
        return datesource;
    }

    /**
     * 获得 element节点下，名为NodeRawname的元素节点的内容
     * 
     * @param NodeRawname
     *            元素节点的节点名
     * @param element
     *            当前节点对象
     * @return String对象
     */
    public static String getNodeContent(String NodeRawname, Element element) {
        Node tmpNode = element.getElementsByTagName(NodeRawname).item(0);
        if (tmpNode != null)
            return tmpNode.getTextContent().trim();
        else
            return null;
    }

    /**
     * 获得 RemoteWeatherforecast 实例的方法
     * 
     * @param forecastNode
     *            forecast节点对象
     * @return RemoteWeatherforecast对象
     */
    private static RemoteWeatherForecast getForecast(Element forecastNode) {

        RemoteWeatherForecast forecast = null;
        if (forecastNode != null) {
            forecast = new RemoteWeatherForecast();
            Node dateNode = forecastNode.getElementsByTagName(
                    RemoteWeatherForecast.RAWDATE).item(0);
            if (dateNode != null) {
                Node dateattr = dateNode.getAttributes().getNamedItem(
                        RemoteWeatherForecast.ATTRDATE);
                forecast.mDate = dateattr.getTextContent().trim();
            }
            Element conditonNode = (Element) forecastNode.getElementsByTagName(
                    RemoteWeatherForecast.RAWCONDITION).item(0);
            forecast.mCondition = getCondition(conditonNode);
            forecast.mHigh = getNodeContent(RemoteWeatherForecast.RAWHIGH,
                    forecastNode);
            forecast.mHumidity = getNodeContent(
                    RemoteWeatherForecast.RAWHUMIDITY, forecastNode);
            forecast.mLow = getNodeContent(RemoteWeatherForecast.RAWLOW,
                    forecastNode);
            forecast.mTemp = getNodeContent(RemoteWeatherForecast.RAWTEMP,
                    forecastNode);
            forecast.mWind = getNodeContent(RemoteWeatherForecast.RAWWIND,
                    forecastNode);
        }
        return forecast;
    }

    /**
     * 获得RemoteWeatherCondition实例的方法
     * 
     * @param conditonNode
     *            condition节点
     * @return RemoteWeatherCondition对象
     */
    private static RemoteWeatherCondition getCondition(Element conditonNode) {
        RemoteWeatherCondition condition = new RemoteWeatherCondition();
        condition.mDescription = getNodeContent(
                RemoteWeatherCondition.RAWDESCRIPTION, conditonNode);
        condition.mBgImage = getNodeContent(RemoteWeatherCondition.RAWBGIMAGE,
                conditonNode);
        condition.mImage = getNodeContent(RemoteWeatherCondition.RAWIMAGE,
                conditonNode);
        return condition;
    }

    /**
     * 获得RemoteShopDatasource实例的方法
     * 
     * @param shopNode
     *            shop节点对象
     * @return RemoteShopDatasource对象
     */
    private static RemoteShopDatasource getShop(Element shopNode) {

        RemoteShopDatasource shop = null;
        if (shopNode != null) {
            shop = new RemoteShopDatasource();
            shop.mId = getNodeContent(RemoteShopDatasource.ATTRID, shopNode);
            shop.mName = getNodeContent(RemoteShopDatasource.ATTRNAME, shopNode);
            shop.mBranchName = getNodeContent(
                    RemoteShopDatasource.RAWBRANCHNAME, shopNode);
            shop.mAddress = getNodeContent(RemoteShopDatasource.RAWADDRESS,
                    shopNode);
            shop.mAvgPrice = getNodeContent(RemoteShopDatasource.RAWAVGPRICE,
                    shopNode);
            shop.mCategory = getNodeContent(RemoteShopDatasource.RAWCATEGORY,
                    shopNode);
            shop.mLatitude = getNodeContent(RemoteShopDatasource.RAWLATITUDE,
                    shopNode);
            shop.mLongitude = getNodeContent(RemoteShopDatasource.RAWLONGITUDE,
                    shopNode);
            shop.mUrl = getNodeContent(RemoteShopDatasource.RAWURL, shopNode);
            shop.mScore = getNodeContent(RemoteShopDatasource.RAWSCORE,
                    shopNode);
            shop.mScoreText = getNodeContent(RemoteShopDatasource.RAWSCORETEXT,
                    shopNode);
            shop.mPic = getNodeContent(RemoteShopDatasource.RAWRPIC, shopNode);
            shop.mDishTags = getNodeContent(RemoteShopDatasource.RAWDISHTAGS,
                    shopNode);
            shop.mShopTags = getNodeContent(RemoteShopDatasource.RAWSHOPTAGS,
                    shopNode);
            Element numbernode = (Element) shopNode.getElementsByTagName(
                    RemoteShopDatasource.RAWPHONENUMBERS).item(0);
            NodeList numberlist = numbernode
                    .getElementsByTagName(RemoteShopDatasource.RAWPHONENUMBER);
            int numberlenth = numberlist.getLength();
            shop.mPhoneNumbers = new ArrayList<String>();
            if (numberlist != null) {
                for (int i = 0; i < numberlenth; i++) {
                    Element number = (Element) numberlist.item(i);
                    shop.mPhoneNumbers.add(number.getTextContent().trim());
                }
            }
        }
        return shop;
    }

    /**
     * 获取RemoteNews的实例的方法
     * 
     * @param newsNode
     *            news节点
     * @return RemoteNews对象
     */
    private static RemoteNews getNews(Element newsNode) {
        RemoteNews news = null;
        if (newsNode != null) {
            news = new RemoteNews();
            news.mType = newsNode.getAttribute(RemoteNews.ATTRTYPE);
            news.mNewsiTems = getNewsItem(newsNode);
        }
        return news;
    }

    /**
     * 获取RemoteNewsItem实例的方法
     * 
     * @param itemsNode
     *            item节点对象
     * @return RemoteNewsItem对象
     */
    private static List<RemoteNewsItem> getNewsItem(Element itemsNode) {
        List<RemoteNewsItem> listitem = new ArrayList<RemoteNewsItem>();
        NodeList Nodelist = itemsNode.getElementsByTagName(RemoteNews.RAWITEM);
        int lenth = Nodelist.getLength();
        for (int i = 0; i < lenth; i++) {
            Element itmeNode = (Element) Nodelist.item(i);
            RemoteNewsItem item = new RemoteNewsItem();
            item.mTitle = getNodeContent(RemoteNewsItem.RAWTITLE, itmeNode);
            item.mMarkedTitle = getNodeContent(RemoteNewsItem.RAWMARKEDTITLE,
                    itmeNode);
            item.mContent = getNodeContent(RemoteNewsItem.RAWCONTENT, itmeNode);
            item.mMarkedcontent = getNodeContent(
                    RemoteNewsItem.RAWMARKEDCONTENT, itmeNode);
            item.mSource = getNodeContent(RemoteNewsItem.RAWSOURCE, itmeNode);
            item.mUrl = getNodeContent(RemoteNewsItem.RAWURL, itmeNode);
            listitem.add(item);
        }
        return listitem;
    }

    /**
     * 获取 RemoteStockDate实例的方法
     * 
     * @param dataNode
     *            股票的data节点对象
     * @return RemoteStockDate对象
     */
    private static RemoteStockDate getStockData(Element dataNode) {

        RemoteStockDate stockdate = null;
        if (dataNode != null) {
            stockdate = new RemoteStockDate();
            stockdate.mCurrentPrice = getNodeContent(
                    RemoteStockDate.RAWCURRENTPRICE, dataNode);
            stockdate.mClosingPrice = getNodeContent(
                    RemoteStockDate.RAWCLOSINGPRICE, dataNode);
            stockdate.mOpeningPrice = getNodeContent(
                    RemoteStockDate.RAWOPENINGPRICE, dataNode);
            stockdate.mHighPrice = getNodeContent(RemoteStockDate.RAWHIGHPRICE,
                    dataNode);
            stockdate.mLowPrice = getNodeContent(RemoteStockDate.RAWLOWPRICE,
                    dataNode);
            stockdate.mRiseValue = getNodeContent(RemoteStockDate.RAWRISEVALUE,
                    dataNode);
            stockdate.mRiseRate = getNodeContent(RemoteStockDate.RAWRISERATE,
                    dataNode);
            Node update_datetime = dataNode.getElementsByTagName(
                    RemoteStockDate.RAWUPDATEDATETIME).item(0);
            stockdate.mUpdateDateTime = getNoteDatetime(update_datetime);
            stockdate.mMbmChartUrl = getNodeContent(
                    RemoteStockDate.RAWMBMCHART_URL, dataNode);
        }
        return stockdate;
    }
}
